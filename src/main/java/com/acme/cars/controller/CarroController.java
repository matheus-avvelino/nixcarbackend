package com.acme.cars.controller;

import com.acme.cars.exception.RecursoNaoEncontradoException;
import com.acme.cars.model.Carro;
import com.acme.cars.payload.CriteriaRequest;
import com.acme.cars.service.CarroService;
import com.acme.cars.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController  @RequestMapping("/api/carros")
@RequiredArgsConstructor  @CrossOrigin(origins = "*",allowedHeaders = "*",exposedHeaders = "*") //Adicionado CORE Headers
public class CarroController {
    private static final Logger logger = Logger.getLogger(CarroController.class.getName());
    private final CarroService carroService;
    private final CsvService csvService;


    @GetMapping("/search")
    public ResponseEntity<List<Carro>> search(
            @RequestHeader(value = "modelo", required = false) Optional<String> modelo,
            @RequestHeader(value = "fabricante", required = false) Optional<String> fabricante,
            @RequestHeader(value = "pais", required = false) Optional<String> pais) {

        CriteriaRequest criteriaRequest = new CriteriaRequest(modelo, fabricante,pais);
        List<Carro> search = carroService.search(criteriaRequest);
        return ResponseEntity.ok(search);
    }
    @GetMapping
    public ResponseEntity<List<Carro>> listarTodos(@RequestHeader(value = "page", defaultValue = "0") String page,
                                                   @RequestHeader(value = "size", defaultValue = "10") String size) {
        logger.info("CARRO CONTAGEM: {}"+ carroService.count());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(carroService.count()));
        List<Carro> allCarros = carroService.getAllPaginado(Integer.parseInt(page), Integer.parseInt(size));
        return new ResponseEntity<>(allCarros, headers, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Carro> buscarPorId(@PathVariable Long id) {
        try {
            Carro carro = carroService.buscarPorId(id);
            return ResponseEntity.ok(carro);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<Carro> salvar(@RequestBody Carro carro) {
        Carro carroSalvo = carroService.salvar(carro);
        return ResponseEntity.status(HttpStatus.CREATED).body(carroSalvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizar(@PathVariable Long id, @RequestBody Carro carroAtualizado) {
        try {
            Carro carro = carroService.atualizar(id, carroAtualizado);
            return ResponseEntity.ok(carro);
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            carroService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/export-cars")
    public ResponseEntity<FileSystemResource> exportCharacters() {
        String filePath = "carros.csv";
        csvService.generate(filePath);
        File file = new File(filePath);
        FileSystemResource fileSystemResource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=" + file.getName())
                .body(fileSystemResource);
    }
}
