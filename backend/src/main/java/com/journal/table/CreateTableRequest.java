package com.journal.table;

import java.util.List;

public record CreateTableRequest(String name, List<CreateColumnRequest> columns) {}
