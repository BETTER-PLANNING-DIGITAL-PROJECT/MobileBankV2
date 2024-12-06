package ibnk.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataTable {
    int totalPages;

    Long totalElements;

    List data;
}
