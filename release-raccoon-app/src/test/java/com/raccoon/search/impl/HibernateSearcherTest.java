package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.dto.mapping.ArtistMapper;

import org.hibernate.search.mapper.orm.session.SearchSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class HibernateSearcherTest {

    HibernateSearcher searcher;

    @Mock
    SearchSession mockSearchSession;
    @Mock
    ArtistMapper mockArtistMapper;

    @BeforeEach
    void setUp() {
        searcher = new HibernateSearcher(mockSearchSession, mockArtistMapper);
    }

    @Test
    @DisplayName("id()")
    void id() {
        assertEquals(Constants.HIBERNATE_SEARCHER_ID, searcher.id());
    }

    @Test
    @DisplayName("trustworthiness()")
    void trustworthiness() {
        assertEquals(Constants.HIBERNATE_SEARCHER_TRUSTWORTHINESS, searcher.trustworthiness());
    }

}
