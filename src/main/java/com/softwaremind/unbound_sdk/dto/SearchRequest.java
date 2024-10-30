package com.softwaremind.unbound_sdk.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchRequest {

    private String baseDN;

    private List<String> attributes;
}
