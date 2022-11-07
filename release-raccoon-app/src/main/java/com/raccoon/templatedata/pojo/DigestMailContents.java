package com.raccoon.templatedata.pojo;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.Release;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DigestMailContents {

    private RaccoonUser raccoonUser;
    private String mailTitle;
    private List<Release> releases;

}
