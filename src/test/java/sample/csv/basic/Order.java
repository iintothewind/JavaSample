package sample.csv.basic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    String trackNumber;
    String orderId;
    String title;
    String phoneNumber;
    String email;
    String zipCode;
    String address;
    String notes;
    String consent;
    String weight;
    String length;
    String width;
    String height;
}
