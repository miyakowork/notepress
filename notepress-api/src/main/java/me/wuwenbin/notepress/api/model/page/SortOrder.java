package me.wuwenbin.notepress.api.model.page;

import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * @author wuwenbin
 */
@Data
@Builder
public class SortOrder {

    private String order;
    private String sort;

    public static SortOrder me(String order, String sort) {
        return SortOrder.builder().order(order).sort(sort).build();
    }

    public static List<SortOrder> our(SortOrder... sortOrder) {
        return Arrays.asList(sortOrder);
    }

}
