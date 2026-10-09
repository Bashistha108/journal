package com.journal.table;

import java.util.List;

public record TableDetail(Long id, String displayName, List<ColumnDetail> columns) {}
