package org.nomiky.nomikyframework.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class R<T> implements Serializable {

    private Integer code;
    private String desc;
    private T data;

    public static R<?> ok() {
        return new R<>(200, "成功！", null);
    }

    public static <T> R<T> data(T data) {
        return new R<>(200, "成功", data);
    }

    public static R<?> fail() {
        return new R<>(500, "失败！", null);
    }

    public static R<?> fail(int code) {
        return new R<>(code, "失败！", null);
    }

    public static R<?> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    public static R<?> fail(String message) {
        return new R<>(500, message, null);
    }
}
