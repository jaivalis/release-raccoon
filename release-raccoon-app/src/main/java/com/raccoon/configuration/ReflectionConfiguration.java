package com.raccoon.configuration;

import com.neovisionaries.i18n.CountryCode;
import com.raccoon.common.ExcludeFromJacocoGeneratedReport;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * CountryCode is used by Gson upon deserialization of AlbumSimplified objects on new release scraping
 */
@RegisterForReflection(targets = {
        AlbumSimplified.class,
        CountryCode.class
})
@ExcludeFromJacocoGeneratedReport
public class ReflectionConfiguration {}
