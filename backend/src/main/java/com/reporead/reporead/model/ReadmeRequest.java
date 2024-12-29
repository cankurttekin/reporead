package com.reporead.reporead.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadmeRequest {
    private String repoUrl;
    private String tone;
    private String language;
    private Boolean badges;

}

