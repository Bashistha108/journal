package com.journal.table;

import java.util.List;

public record CreateColumnRequest(String name, String dataType, List<String> selectOptions) {}
