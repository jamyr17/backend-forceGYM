package una.force_gym.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import una.force_gym.domain.EconomicIncome;
import una.force_gym.dto.EconomicIncomeDTO;
import una.force_gym.dto.ParamLoggedIdUserDTO;
import una.force_gym.exception.AppException;
import una.force_gym.service.EconomicIncomeService;
import una.force_gym.util.ApiResponse;


@RestController
@RequestMapping("/economicIncome")
public class EconomicIncomeController {

    @Autowired
    private EconomicIncomeService economicIncomeService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEconomicIncomes(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            List<EconomicIncome> economicIncomes = economicIncomeService.getEconomicIncomes(page, size);
            Long totalRecords = economicIncomeService.countActiveIncomes();

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("economicIncomes", economicIncomes);
            responseData.put("totalRecords", totalRecords);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ingresos económicos obtenidos correctamente.", responseData);
            return new ResponseEntity<>(response, HttpStatus.OK); 

        } catch (RuntimeException e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ocurrió un error al solicitar los datos de los ingresos económicos.", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); 
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addEconomicIncome(@RequestBody EconomicIncomeDTO economicIncomeDTO) {
        int result = economicIncomeService.addEconomicIncome(
            economicIncomeDTO.getIdUser(), 
            economicIncomeDTO.getRegistrationDate(), 
            economicIncomeDTO.getVoucherNumber(), 
            economicIncomeDTO.getDetail(), 
            economicIncomeDTO.getIdMeanOfPayment(), 
            economicIncomeDTO.getAmount(), 
            economicIncomeDTO.getIdActivityType(), 
            economicIncomeDTO.getParamLoggedIdUser()
        );

        switch(result) {
            case 1 -> 
            { 
                ApiResponse<String> response = new ApiResponse<>("Ingreso económico agregado correctamente.", null);
                return new ResponseEntity<>(response, HttpStatus.OK); 
            }

            // error de MySQL
            case 0 -> throw new AppException("Ocurrió un error al agregar el nuevo ingreso económico.", HttpStatus.INTERNAL_SERVER_ERROR);
            
            // no se encuentra el idUser
            case -1 -> throw new AppException("No se pudo agregar el nuevo ingreso económico debido a que el usuario asociado no está registrado.", HttpStatus.INTERNAL_SERVER_ERROR);
            
            default -> throw new AppException("Ingreso económico no agregado debido a problemas en la consulta.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<String>> updateEconomicIncome(@RequestBody EconomicIncomeDTO economicIncomeDTO) {
        int result = economicIncomeService.updateEconomicIncome(
            economicIncomeDTO.getIdEconomicIncome(), 
            economicIncomeDTO.getIdUser(), 
            economicIncomeDTO.getRegistrationDate(), 
            economicIncomeDTO.getVoucherNumber(), 
            economicIncomeDTO.getDetail(), 
            economicIncomeDTO.getIdMeanOfPayment(), 
            economicIncomeDTO.getAmount(), 
            economicIncomeDTO.getIdActivityType(), 
            economicIncomeDTO.getParamLoggedIdUser()
        );

        switch(result) {
            case 1 -> 
            { 
                ApiResponse<String> response = new ApiResponse<>("Ingreso económico actualizado correctamente.", null);
                return new ResponseEntity<>(response, HttpStatus.OK); 
            }

            // error de MySQL
            case 0 -> throw new AppException("Ocurrió un error al actualizar el ingreso económico.", HttpStatus.INTERNAL_SERVER_ERROR);

            // no se encuentra el idEconomicIncome
            case -1 -> throw new AppException("No se pudo actualizar el ingreso económico debido a que no se encuentra el registro.", HttpStatus.INTERNAL_SERVER_ERROR);

            // no se encuentra el idUser
            case -2 -> throw new AppException("No se pudo actualizar el ingreso económico debido a que el usuario asociado no está registrado.", HttpStatus.INTERNAL_SERVER_ERROR);
            
            default -> throw new AppException("Ingreso económico no actualizado debido a problemas en la consulta.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/delete/{idEconomicIncome}")
    public ResponseEntity<ApiResponse<String>> deleteEconomicIncome(@PathVariable("idEconomicIncome") Long idEconomicIncome, @RequestBody ParamLoggedIdUserDTO paramLoggedIdUser) {
        int result = economicIncomeService.deleteEconomicIncome(idEconomicIncome, paramLoggedIdUser.getParamLoggedIdUser());
       
        switch(result) {
            case 1 -> 
            { 
                ApiResponse<String> response = new ApiResponse<>("Ingreso económico eliminado correctamente.", null);
                return new ResponseEntity<>(response, HttpStatus.OK); 
            }

            // error de MySQL
            case 0 -> throw new AppException("Ocurrió un error al eliminar el ingreso económico.", HttpStatus.INTERNAL_SERVER_ERROR);

            // no se encuentra el idEconomicIncome
            case -1 -> throw new AppException("No se pudo eliminar el ingreso económico debido a que no se encuentra el registro.", HttpStatus.INTERNAL_SERVER_ERROR);

            default -> throw new AppException("Ingreso económico no eliminado debido a problemas en la consulta.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/incomesByAmountRange")
    public ResponseEntity<ApiResponse<Map<String, Object>>> filterEconomicIncomesByAmountRange(
        @RequestParam("minAmount") double minAmount,
        @RequestParam("maxAmount") double maxAmount,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            List<EconomicIncome> economicIncomes = economicIncomeService.getEconomicIncomesByAmountRange(minAmount, maxAmount, page, size);
            Long totalRecords = (long) economicIncomes.size(); 

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("economicIncomes", economicIncomes);
            responseData.put("totalRecords", totalRecords);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ingresos económicos filtrados por rango de montos obtenidos correctamente.", responseData);
            return new ResponseEntity<>(response, HttpStatus.OK); 
        } catch (RuntimeException e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ocurrió un error al solicitar los datos de los ingresos económicos por rango de montos.", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); 
        }
    }

    @GetMapping("/incomesByDateRange")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEconomicIncomesByDateRange(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size) {
        try {
            List<EconomicIncome> economicIncomes = economicIncomeService.getEconomicIncomesByDateRange(startDate, endDate, page, size);
            Long totalRecords = (long) economicIncomes.size(); 

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("economicIncomes", economicIncomes);
            responseData.put("totalRecords", totalRecords);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ingresos económicos filtrados por rango de fechas obtenidos correctamente.", responseData);
            return new ResponseEntity<>(response, HttpStatus.OK); 
        } catch (RuntimeException e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ocurrió un error al solicitar los datos de los ingresos económicos por rango de fechas.", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); 
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchEconomicIncomes(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<EconomicIncome> economicIncomes = economicIncomeService.searchEconomicIncomes(searchTerm, page, size);
            Long totalRecords = economicIncomeService.countIncomeBySearchTerm(searchTerm);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("economicIncomes", economicIncomes);
            responseData.put("totalRecords", totalRecords);

            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ingresos económicos obtenidos correctamente.", responseData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Ocurrió un error al solicitar los datos de los ingresos económicos.", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
