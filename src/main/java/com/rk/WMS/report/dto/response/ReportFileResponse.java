package com.rk.WMS.report.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportFileResponse {

    private String fileName;

    private byte[] fileContent;
}
