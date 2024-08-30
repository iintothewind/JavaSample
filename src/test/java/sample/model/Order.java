package sample.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    private Long id;
    private String sourceMerchant;
    private String trackNumber;
    private String sourceTrackNumber;
    private String externalOrderCode;
    private Long manifestId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String countryCode;
    private String province;
    private String city;
    private String street;
    private String addressLine2;
    private String postcode;
    private String sourceCountryCode;
    private String sourceProvince;
    private String sourceCity;
    private String sourceStreet;
    private String sourceAddressLine2;
    private String sourcePostcode;
    private String fsaId;
    private Long regionId;
    private Double lat;
    private Double lng;
    private String formattedAddress;
    private String postalAddress;
    private Integer attemptedCount;
    private Integer status;
    private String memo;
    private String deliveryOption;
    private String category;
    private LocalDateTime dueTime;
    /**
     * 重量单位 1 磅 2 公斤
     */
    private Integer weightUnit;
    /**
     * 尺寸单位 1 英寸 2 厘米
     */
    private Integer dimUnit;
    /**
     * 重量
     */
    private BigDecimal weight;
    /**
     * 长
     */
    private BigDecimal length;
    /**
     * 宽
     */
    private BigDecimal width;
    /**
     * 高
     */
    private BigDecimal height;
    /**
     * 重量（重量单位字段代表他的单位）
     */
    private BigDecimal scaleWeight;
    /**
     * 尺寸重量（重量单位字段代表他的单位）
     */
    private BigDecimal dimWeight;
    /**
     * 计费重量（重量单位字段代表他的单位）
     */
    private BigDecimal oriBillingWeight;
    /**
     * 实际/计费重量（单位：镑)
     */
    private BigDecimal billingWeight;
    /**
     * 是否需要签名
     */
    private Boolean signatureNeeded;
    /**
     * 价格（$）
     */
    private BigDecimal totalValue;
    /**
     * 分拣预案ID
     */
    private Long sortPlanId;

    /**
     * 分拣预案编号
     */
    private String sortPlanCode;
    /**
     * 提到货站点
     */
    private Long arriveDeptId;

    /**
     * 站点id（配送）
     */
    private Long deptId;

    /**
     * 客户ID
     */
    private Long clientId;
    /**
     * 地址ID
     * 地址ID
     */
    private Long addressId;

    /**
     * 是否标准件（0：否；1：是）
     */
    private Boolean standard;
    /**
     * 是否周边件（0：否；1：是）
     */
    private Boolean stationNearby;
    /**
     * 是否超大超重件（0：否；1：是）
     */
    private Boolean overWeightedSized;
    /**
     * 是否周边件（0：否；1：是）
     */
    private Boolean dense;
    /**
     * 类型(1-标准件;2-周边件;3-超大超重件;4-密集件；)
     */
    private Integer type;
    /**
     * 合作伙伴track number
     */
    // TODO: change data type to String -Done
    private String partnerTrackNumber;
    /**
     * 合作伙伴track id
     */
    // TODO: change data type to String -Done
    private String partnerTrackId;
    /**
     * 合作伙伴批次
     */
    // TODO: rename to partnerManifestId, change data type to Integer -Done
    private Integer partnerManifestId;
    /**
     * 原备注
     */
    private String remark;
    /**
     * AWB/BOL
     */
    private String awbBol;
    /**
     * 提到货时间
     */
    private LocalDateTime inboundTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 分拣员ID
     */
    private Long inboundUserId;
    /**
     * 分拣员ID
     */
    private Long sorterId;
    /**
     * 路线ID
     */
    private Long routeId;
    /**
     * 司机ID
     */
    private Long driverId;
    /**
     * 运费金额
     */
    private BigDecimal postAmount;
    /**
     * 燃油附加费系数
     */
    private BigDecimal fuelSurcharge;
    /**
     * 签字费
     */
    private BigDecimal signatureFee;
    /**
     * 费用小计
     */
    private BigDecimal totalAmount;
    /**
     * 结算状态： 1 待结算  2 结算中  3 已结算
     */
    private Integer settlementStatus;
    /**
     * 计费节点（和status定义一致）
     */
    private Integer billingNode;
    /**
     * 计费时间
     */
    private LocalDateTime billingTime;
    /**
     * 结算审核通过人ID
     */
    private Long settlementApproveId;
    /**
     * 发件人
     */
    private String sender;

    /**
     * 入库类型（1：正常一扫；2：虚拟一扫）
     */
    private Integer inboundType;
    /**
     * 一扫入库时间
     */
    private LocalDateTime firstScanTime;

    /**
     * 二扫分拣时间
     */
    private LocalDateTime secondScanTime;


    /**
     * 签收时间
     */
    private LocalDateTime signatureTime;
    /**
     * 结算审核通过时间
     */
    private LocalDateTime settlementApproveTime;
    /**
     * 客户结算汇总ID
     */
    private Long clientSettlementSummaryId;
    /**
     * 配送包裹id
     */
    private Long deliveryItemId;
    /**
     * 配送计划id
     */
    private Long deliveryPlanId;

    /**
     * 配送计划编号
     */
    private String deliveryPlanCode;
    /**
     * 客服异常(1-地址异常;2-地址错误；3-FSA范围异常)
     */
    private Integer failType;

    /**
     * 客服异常来源阶段(1-预报；2-配送)
     */
    private Integer failFrom;

    /**
     * 客服异常状态(0:非异常订单；1:异常待处理；2:异常处理中；3:异常已处理)
     */
    private Integer failStatus;

    /**
     * 物理站点
     */
    private Long physicalDeptId;

    /**
     * 导入流水号
     */
    private String importCode;


    /**
     * 创建人ID
     */
    private Long creatorId;
    /**
     * 更新人ID
     */
    private Long operatorId;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
