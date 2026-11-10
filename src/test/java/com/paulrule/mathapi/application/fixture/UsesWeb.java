package com.paulrule.mathapi.application.fixture;

import com.paulrule.mathapi.adapter.in.web.fixture.WebType;
import org.springframework.web.bind.annotation.RequestBody;

/** Breaks the layer and application rules on purpose so {@code ArchitectureRulesAreLiveTest} can prove they fire. */
public class UsesWeb {
    WebType web;
    RequestBody springWeb;
}
