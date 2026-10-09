package com.journal.table;

import java.util.List;

public record ColumnDetail(Long id, String displayName, String dataType, List<SelectOptionDto> selectOptions) {}
