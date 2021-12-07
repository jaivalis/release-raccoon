package com.raccoon.templatedata.pojo;

import com.raccoon.entity.Release;
import com.raccoon.entity.User;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DigestMailContents {

    private User user;
    private String mailTitle;
    private List<Release> releases;

}
