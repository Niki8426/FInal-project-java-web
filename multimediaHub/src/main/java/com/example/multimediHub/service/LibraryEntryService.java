package com.example.multimediHub.service;

import com.example.multimediHub.repository.LibraryEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LibraryEntryService {

    private final LibraryEntryRepository libraryEntryRepository;

    @Autowired
    public LibraryEntryService(LibraryEntryRepository libraryEntryRepository) {
        this.libraryEntryRepository = libraryEntryRepository;
    }
}
