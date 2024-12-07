package org.codec.entity.third;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("business_stats")
public class BusinessStats {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("area_report_id")
    private Long areaReportId;

    @TableField("around_buss_model_count")
    private Integer aroundBussModelCount;

    @TableField("eatery")
    private Integer eatery;

    @TableField("retail")
    private Integer retail;

    @TableField("hotel")
    private Integer hotel;

    @TableField("education")
    private Integer education;

    @TableField("hospital")
    private Integer hospital;

    @TableField("recreation")
    private Integer recreation;

    @TableField("emergency_center")
    private Integer emergencyCenter;

    @TableField("clinical")
    private Integer clinical;

    @TableField("general_hospital")
    private Integer generalHospital;

    @TableField("specialized_hospital")
    private Integer specializedHospital;

    @TableField("medical_insurance")
    private Integer medicalInsurance;

    @TableField("kindergarten")
    private Integer kindergarten;

    @TableField("primary_school")
    private Integer primarySchool;

    @TableField("middle_school")
    private Integer middleSchool;

    @TableField("university")
    private Integer university;

    @TableField("building")
    private Integer building;

    @TableField("households")
    private Integer households;

    @TableField("company")
    private Integer company;

    @TableField("administration_building")
    private Integer administrationBuilding;

    @TableField("internet")
    private Integer internet;

    @TableField("wholesale")
    private Integer wholesale;

    @TableField("financial")
    private Integer financial;

    @TableField("culture")
    private Integer culture;

    @TableField("sports_and_recreation")
    private Integer sportsAndRecreation;

    @TableField("manufacturing")
    private Integer manufacturing;

    @TableField("industry_qita")
    private Integer industryQiTa;

    @TableField("business_circle_number")
    private Integer businessCircleNumber;
}
