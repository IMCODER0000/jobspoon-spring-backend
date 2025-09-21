package com.wowraid.jobspoon.ebook.service.export;

import com.wowraid.jobspoon.ebook.controller.export.request_form.TermsPdfGenerateByFolderRequestForm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface EbookExportApplicationService {
    ResponseEntity<StreamingResponseBody> generateByFolder(Long accountId, TermsPdfGenerateByFolderRequestForm requestForm);
}
