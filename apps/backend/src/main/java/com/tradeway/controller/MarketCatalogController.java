package com.tradeway.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tradeway.dto.InstrumentResponse;
import com.tradeway.model.Instrument;
import com.tradeway.repository.InstrumentRepository;
import com.tradeway.service.TradingService;

@RestController
@RequestMapping("/api")
public class MarketCatalogController {

    private final InstrumentRepository instrumentRepository;
    private final TradingService tradingService;

    public MarketCatalogController(InstrumentRepository instrumentRepository, TradingService tradingService) {
        this.instrumentRepository = instrumentRepository;
        this.tradingService = tradingService;
    }

    @GetMapping("/instruments")
    public List<InstrumentResponse> getInstruments(Authentication authentication) {
        requireAuthenticated(authentication);
        return instrumentRepository.findAll().stream()
            .filter(Instrument::isActive)
            .map(instrument -> InstrumentResponse.from(instrument, tradingService.getQuote(instrument.getSymbol())))
            .toList();
    }

    @GetMapping("/instruments/{symbol}/quote")
    public ResponseEntity<InstrumentResponse> getQuote(@PathVariable String symbol, Authentication authentication) {
        requireAuthenticated(authentication);
        Instrument instrument = instrumentRepository.findBySymbolIgnoreCase(symbol)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument not found"));
        return ResponseEntity.ok(InstrumentResponse.from(instrument, tradingService.getQuote(instrument.getSymbol())));
    }

    private void requireAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
    }
}
