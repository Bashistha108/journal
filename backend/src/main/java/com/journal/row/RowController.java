package com.journal.row;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import com.journal.attachment.AttachmentChangeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tables/{tableId}/rows")
public class RowController {
    
    private final RowService rowService;

    public RowController(RowService rowService) {
        this.rowService = rowService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RowDetail createRowJson(@PathVariable Long tableId, @RequestBody RowSaveRequest request) {
        return rowService.createRow(tableId, request, null);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RowDetail createRowMultipart(@PathVariable Long tableId,
                                        @RequestPart("row") RowSaveRequest request,
                                        MultipartHttpServletRequest multipartRequest) {
        return rowService.createRow(tableId, request, buildChangeSet(request, multipartRequest));
    }

    @GetMapping("/{rowId}")
    public RowDetail getRow(@PathVariable Long tableId, @PathVariable Long rowId) {
        return rowService.getRow(tableId, rowId);
    }

    @PatchMapping(value = "/{rowId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RowDetail updateRowJson(@PathVariable Long tableId, @PathVariable Long rowId, @RequestBody RowSaveRequest request) {
        return rowService.updateRow(tableId, rowId, request, null);
    }

    @PostMapping(value = "/{rowId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RowDetail updateRowMultipart(@PathVariable Long tableId, @PathVariable Long rowId,
                                        @RequestPart("row") RowSaveRequest request,
                                        MultipartHttpServletRequest multipartRequest) {
        return rowService.updateRow(tableId, rowId, request, buildChangeSet(request, multipartRequest));
    }

    private AttachmentChangeSet buildChangeSet(RowSaveRequest request, MultipartHttpServletRequest multipartRequest) {
        List<Long> removeIds = request.getRemoveAttachmentIds() != null ? request.getRemoveAttachmentIds() : List.of();
        Map<Long, List<MultipartFile>> additions = new HashMap<>();
        if (request.getNewAttachments() != null) {
            for (var newAtt : request.getNewAttachments()) {
                MultipartFile file = multipartRequest.getFile(newAtt.getClientRef());
                if (file != null) {
                    additions.computeIfAbsent(newAtt.getColumnId(), k -> new ArrayList<>()).add(file);
                }
            }
        }
        return new AttachmentChangeSet(removeIds, additions);
    }

    @DeleteMapping("/{rowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRow(@PathVariable Long tableId, @PathVariable Long rowId) {
        rowService.deleteRow(tableId, rowId);
    }
}
