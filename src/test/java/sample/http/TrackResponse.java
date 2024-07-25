package sample.http;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TrackResponse {

    String courierName;
    String currentStatus;
    String currentStatusCode;

    Date date;

    Boolean signatureNeeded;

    String trackNumber;
    String message;
    String result;
    Integer status;
}
