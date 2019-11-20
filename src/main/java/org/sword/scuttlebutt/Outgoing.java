package org.sword.scuttlebutt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author tiyxing
 * @date 2019-11-10
 * @since 1.0.0
 */
@ToString
@AllArgsConstructor
@Getter
@Setter
public class Outgoing {
    private String sourceId;

    private Map<String,Long> source;

}
