package org.sword.scuttlebutt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author tiyxing
 * @date 2019-11-10
 * @since 1.0.0
 */
@ToString
@AllArgsConstructor
@Setter
@Getter
public class BizData<T> {
    private String key;

    private T value;
}
