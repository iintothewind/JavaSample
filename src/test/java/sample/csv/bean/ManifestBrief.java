package sample.csv.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManifestBrief {
    private String manifest;
    private Integer route;
    private Integer driver;
    private Integer number;

}
