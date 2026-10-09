package com.journal.table;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tables/{tableId}/columns/{columnId}/select-options")
public class SelectOptionController {
    private final SelectOptionService selectOptionService;

    public SelectOptionController(SelectOptionService selectOptionService) {
        this.selectOptionService = selectOptionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addOptions(@PathVariable Long tableId, @PathVariable Long columnId, @RequestBody AddSelectOptionsRequest request) {
        selectOptionService.addOptions(tableId, columnId, request);
    }

    @DeleteMapping("/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOption(@PathVariable Long tableId, @PathVariable Long columnId, @PathVariable Long optionId) {
        selectOptionService.deleteOption(tableId, columnId, optionId);
    }
}
