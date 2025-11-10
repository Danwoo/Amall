package project.amall.product.dto.response;

import lombok.Builder;
import lombok.Getter;
import project.amall.product.dto.ProductDto;

/**
 * 상품 응답 DTO
 */
@Getter
@Builder
public class ProductResponse {

    private Integer prodNum;
    private String prodName;
    private String prodCode;
    private Integer prodPrice;
    private Integer prodStock;
    private String prodExplanation;
    private String prodRegDate;
    private Integer prodHit;
    private String prodDetail;
    private String prodVal1;
    private String prodVal2;
    private String prodVal3;
    private Integer prodStar;
    private Integer prodShow;
    private String sellerId;
    private Integer categoryId;
    private String prodImage1;
    private String prodImage2;
    private String prodImage3;
    private Integer prodDeliveryfee;

    /**
     * ProductDto를 ProductResponse로 변환
     */
    public static ProductResponse from(ProductDto dto) {
        if (dto == null) {
            return null;
        }

        return ProductResponse.builder()
                .prodNum(dto.getProdNum())
                .prodName(dto.getProdName())
                .prodCode(dto.getProdCode())
                .prodPrice(dto.getProdPrice())
                .prodStock(dto.getProdStock())
                .prodExplanation(dto.getProdExplanation())
                .prodRegDate(dto.getProdRegDate())
                .prodHit(dto.getProdHit())
                .prodDetail(dto.getProdDetail())
                .prodVal1(dto.getProdVal1())
                .prodVal2(dto.getProdVal2())
                .prodVal3(dto.getProdVal3())
                .prodStar(dto.getProdStar())
                .prodShow(dto.getProdShow())
                .sellerId(dto.getSellerId())
                .categoryId(dto.getCategoryId())
                .prodImage1(dto.getProdImage1())
                .prodImage2(dto.getProdImage2())
                .prodImage3(dto.getProdImage3())
                .prodDeliveryfee(dto.getProdDeliveryfee())
                .build();
    }
}
