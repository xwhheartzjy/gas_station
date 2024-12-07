package org.codec.service.third;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.codec.entity.third.*;
import org.codec.mapper.thrid.*;
import org.codec.util.FileReaderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import java.nio.file.Files;

@Service
public class AreaReportService {

    @Autowired
    private AreaReportMapper areaReportMapper;

    @Autowired
    private InfrastructureStatsMapper infrastructureStatsMapper;

    @Autowired
    private BusinessStatsMapper businessStatsMapper;

    @Autowired
    private TrafficStatsMapper trafficStatsMapper;

    @Autowired
    private DwellingDetailMapper dwellingDetailMapper;

    public void processJsonAndSave(String jsonFilePath) throws Exception {
        try {
            // 1. 读取 JSON 文件
            String jsonContent = FileReaderUtil.readFileAsString(jsonFilePath);
            JSONObject jsonObject = JSON.parseObject(jsonContent);
            String fileName = Paths.get(jsonFilePath).getFileName().toString();

            if (jsonObject.getInteger("code") == 200) {
                JSONObject data = jsonObject.getJSONObject("data");

                // 2. 插入主表
                AreaReport areaReport = new AreaReport();
                areaReport.setCreateTime(new Date());
                areaReport.setUpdateTime(new Date());
                areaReport.setVersion(1);
                areaReport.setMessage(jsonObject.getString("message"));
                areaReport.setTraceId(jsonObject.getString("traceId"));
                areaReport.setGasStationName(fileName); // 设置文件名
                areaReportMapper.insert(areaReport);

                Long areaReportId = areaReport.getId();

                // 3. 插入基础设施数据
                InfrastructureStats infrastructureStats = JSON.parseObject(
                        data.getJSONObject("infrastructureDto").toJSONString(),
                        InfrastructureStats.class
                );
                infrastructureStats.setAreaReportId(areaReportId);
                infrastructureStatsMapper.insert(infrastructureStats);

                // 4. 插入业态数据
                BusinessStats businessStats = JSON.parseObject(
                        data.getJSONObject("businessDto").toJSONString(),
                        BusinessStats.class
                );
                businessStats.setAreaReportId(areaReportId);
                businessStatsMapper.insert(businessStats);

                // 5. 插入交通数据
                TrafficStats trafficStats = JSON.parseObject(
                        data.getJSONObject("trafficDto").toJSONString(),
                        TrafficStats.class
                );
                trafficStats.setAreaReportId(areaReportId);
                trafficStatsMapper.insert(trafficStats);

                // 6. 插入小区明细数据
                JSONArray dwellingDetails = data.getJSONArray("dwellingDetailDtoList");
                for (int i = 0; i < dwellingDetails.size(); i++) {
                    DwellingDetail dwellingDetail = JSON.parseObject(
                            dwellingDetails.getJSONObject(i).toJSONString(),
                            DwellingDetail.class
                    );
                    dwellingDetail.setAreaReportId(areaReportId);
                    dwellingDetailMapper.insert(dwellingDetail);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
