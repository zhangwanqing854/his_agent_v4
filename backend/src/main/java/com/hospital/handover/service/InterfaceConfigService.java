package com.hospital.handover.service;

import com.hospital.handover.dto.*;
import com.hospital.handover.entity.InterfaceConfig;
import com.hospital.handover.entity.InterfaceFieldMapping;
import com.hospital.handover.entity.InterfaceMappingTable;
import com.hospital.handover.repository.InterfaceConfigRepository;
import com.hospital.handover.repository.InterfaceFieldMappingRepository;
import com.hospital.handover.repository.InterfaceMappingTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InterfaceConfigService {

    private final InterfaceConfigRepository configRepository;
    private final InterfaceMappingTableRepository mappingTableRepository;
    private final InterfaceFieldMappingRepository fieldMappingRepository;

    public InterfaceConfigService(InterfaceConfigRepository configRepository,
                                  InterfaceMappingTableRepository mappingTableRepository,
                                  InterfaceFieldMappingRepository fieldMappingRepository) {
        this.configRepository = configRepository;
        this.mappingTableRepository = mappingTableRepository;
        this.fieldMappingRepository = fieldMappingRepository;
    }

    public List<InterfaceConfigDto> getAllConfigs() {
        List<InterfaceConfig> configs = configRepository.findAll();
        List<InterfaceConfigDto> result = new ArrayList<>();
        
        for (InterfaceConfig config : configs) {
            result.add(toDtoWithMappings(config));
        }
        
        return result;
    }

    public InterfaceConfigDto getConfigById(Long id) {
        Optional<InterfaceConfig> config = configRepository.findById(id);
        return config.map(this::toDtoWithMappings).orElse(null);
    }

    @Transactional
    public InterfaceConfigDto createConfig(InterfaceConfigDto dto) {
        if (configRepository.findByConfigCode(dto.getConfigCode()) != null) {
            throw new RuntimeException("接口编码已存在");
        }
        
        InterfaceConfig config = new InterfaceConfig();
        copyDtoToEntity(dto, config);
        
        InterfaceConfig saved = configRepository.save(config);
        
        if (dto.getMappingTables() != null) {
            for (MappingTableDto tableDto : dto.getMappingTables()) {
                createMappingTable(saved.getId(), tableDto);
            }
        }
        
        return toDtoWithMappings(saved);
    }

    @Transactional
    public InterfaceConfigDto updateConfig(Long id, InterfaceConfigDto dto) {
        Optional<InterfaceConfig> optional = configRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("接口配置不存在");
        }
        
        InterfaceConfig config = optional.get();
        
        if (dto.getConfigCode() != null && !dto.getConfigCode().equals(config.getConfigCode())) {
            InterfaceConfig existing = configRepository.findByConfigCode(dto.getConfigCode());
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("接口编码已存在");
            }
        }
        
        copyDtoToEntity(dto, config);
        InterfaceConfig saved = configRepository.save(config);
        
        if (dto.getMappingTables() != null) {
            deleteMappingsByConfigId(id);
            
            for (MappingTableDto tableDto : dto.getMappingTables()) {
                createMappingTable(saved.getId(), tableDto);
            }
        }
        
        return toDtoWithMappings(saved);
    }

    @Transactional
    public void deleteConfig(Long id) {
        Optional<InterfaceConfig> optional = configRepository.findById(id);
        
        if (optional.isEmpty()) {
            throw new RuntimeException("接口配置不存在");
        }
        
        deleteMappingsByConfigId(id);
        configRepository.deleteById(id);
    }
    
    private void deleteMappingsByConfigId(Long configId) {
        List<InterfaceMappingTable> tables = mappingTableRepository.findByConfigId(configId);
        for (InterfaceMappingTable table : tables) {
            fieldMappingRepository.deleteByMappingTableId(table.getId());
        }
        mappingTableRepository.deleteByConfigId(configId);
    }

    private void createMappingTable(Long configId, MappingTableDto dto) {
        InterfaceMappingTable table = new InterfaceMappingTable();
        table.setConfigId(configId);
        table.setMappingCode(dto.getMappingCode());
        table.setMappingName(dto.getMappingName());
        table.setTargetTable(dto.getTargetTable());
        table.setDataPath(dto.getDataPath());
        table.setIsArray(dto.getIsArray() != null ? dto.getIsArray() : false);
        table.setParentMappingId(dto.getParentMappingId());
        table.setRelationField(dto.getRelationField());
        table.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        
        InterfaceMappingTable saved = mappingTableRepository.save(table);
        
        if (dto.getFieldMappings() != null) {
            for (FieldMappingDto fieldDto : dto.getFieldMappings()) {
                createFieldMapping(saved.getId(), fieldDto);
            }
        }
    }

    private void createFieldMapping(Long mappingTableId, FieldMappingDto dto) {
        InterfaceFieldMapping field = new InterfaceFieldMapping();
        field.setMappingTableId(mappingTableId);
        field.setSourceField(dto.getSourceField());
        field.setTargetField(dto.getTargetField());
        field.setTransformType(dto.getTransformType() != null ? dto.getTransformType() : "DIRECT");
        field.setTransformConfig(dto.getTransformConfig());
        field.setDefaultValue(dto.getDefaultValue());
        field.setIsRequired(dto.getIsRequired() != null ? dto.getIsRequired() : true);
        field.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        
        fieldMappingRepository.save(field);
    }

    private InterfaceConfigDto toDto(InterfaceConfig config) {
        InterfaceConfigDto dto = new InterfaceConfigDto();
        copyEntityToDto(config, dto);
        return dto;
    }

    private InterfaceConfigDto toDtoWithMappings(InterfaceConfig config) {
        InterfaceConfigDto dto = toDto(config);
        
        List<InterfaceMappingTable> tables = mappingTableRepository.findByConfigId(config.getId());
        
        List<MappingTableDto> tableDtos = new ArrayList<>();
        for (InterfaceMappingTable table : tables) {
            MappingTableDto tableDto = new MappingTableDto();
            tableDto.setId(table.getId());
            tableDto.setConfigId(table.getConfigId());
            tableDto.setMappingCode(table.getMappingCode());
            tableDto.setMappingName(table.getMappingName());
            tableDto.setTargetTable(table.getTargetTable());
            tableDto.setDataPath(table.getDataPath());
            tableDto.setIsArray(table.getIsArray());
            tableDto.setParentMappingId(table.getParentMappingId());
            tableDto.setRelationField(table.getRelationField());
            tableDto.setSortOrder(table.getSortOrder());
            
            List<InterfaceFieldMapping> fields = fieldMappingRepository.findByMappingTableId(table.getId());
            
            List<FieldMappingDto> fieldDtos = new ArrayList<>();
            for (InterfaceFieldMapping field : fields) {
                FieldMappingDto fieldDto = new FieldMappingDto();
                fieldDto.setId(field.getId());
                fieldDto.setMappingTableId(field.getMappingTableId());
                fieldDto.setSourceField(field.getSourceField());
                fieldDto.setTargetField(field.getTargetField());
                fieldDto.setTransformType(field.getTransformType());
                fieldDto.setTransformConfig(field.getTransformConfig());
                fieldDto.setDefaultValue(field.getDefaultValue());
                fieldDto.setIsRequired(field.getIsRequired());
                fieldDto.setSortOrder(field.getSortOrder());
                fieldDtos.add(fieldDto);
            }
            
            tableDto.setFieldMappings(fieldDtos);
            tableDtos.add(tableDto);
        }
        
        dto.setMappingTables(tableDtos);
        return dto;
    }

    private void copyDtoToEntity(InterfaceConfigDto dto, InterfaceConfig entity) {
        if (dto.getConfigCode() != null) entity.setConfigCode(dto.getConfigCode());
        if (dto.getConfigName() != null) entity.setConfigName(dto.getConfigName());
        if (dto.getSystem() != null) entity.setSystem(dto.getSystem());
        if (dto.getMode() != null) entity.setMode(dto.getMode());
        if (dto.getProtocol() != null) entity.setProtocol(dto.getProtocol());
        if (dto.getApiProtocol() != null) entity.setApiProtocol(dto.getApiProtocol());
        if (dto.getMethod() != null) entity.setMethod(dto.getMethod());
        if (dto.getUrl() != null) entity.setUrl(dto.getUrl());
        if (dto.getDataType() != null) entity.setDataType(dto.getDataType());
        if (dto.getSyncMode() != null) entity.setSyncMode(dto.getSyncMode());
        if (dto.getSyncSchedule() != null) entity.setSyncSchedule(dto.getSyncSchedule());
        if (dto.getRequestTemplate() != null) entity.setRequestTemplate(dto.getRequestTemplate());
        if (dto.getAuthType() != null) entity.setAuthType(dto.getAuthType());
        if (dto.getAuthConfig() != null) entity.setAuthConfig(dto.getAuthConfig());
        if (dto.getRetryInterval() != null) entity.setRetryInterval(dto.getRetryInterval());
        if (dto.getMaxRetries() != null) entity.setMaxRetries(dto.getMaxRetries());
        if (dto.getOnFailure() != null) entity.setOnFailure(dto.getOnFailure());
        if (dto.getSyncTimeParamStart() != null) entity.setSyncTimeParamStart(dto.getSyncTimeParamStart());
        if (dto.getSyncTimeParamEnd() != null) entity.setSyncTimeParamEnd(dto.getSyncTimeParamEnd());
        if (dto.getSyncTimeFormat() != null) entity.setSyncTimeFormat(dto.getSyncTimeFormat());
        if (dto.getFirstSyncDays() != null) entity.setFirstSyncDays(dto.getFirstSyncDays());
        if (dto.getIncrementalSyncHours() != null) entity.setIncrementalSyncHours(dto.getIncrementalSyncHours());
        if (dto.getEnabled() != null) entity.setEnabled(dto.getEnabled());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getSoapAction() != null) entity.setSoapAction(dto.getSoapAction());
        if (dto.getSoapNamespace() != null) entity.setSoapNamespace(dto.getSoapNamespace());
        if (dto.getSoapParams() != null) entity.setSoapParams(dto.getSoapParams());
    }

    private void copyEntityToDto(InterfaceConfig entity, InterfaceConfigDto dto) {
        dto.setId(entity.getId());
        dto.setConfigCode(entity.getConfigCode());
        dto.setConfigName(entity.getConfigName());
        dto.setSystem(entity.getSystem());
        dto.setMode(entity.getMode());
        dto.setProtocol(entity.getProtocol());
        dto.setApiProtocol(entity.getApiProtocol());
        dto.setMethod(entity.getMethod());
        dto.setUrl(entity.getUrl());
        dto.setDataType(entity.getDataType());
        dto.setSyncMode(entity.getSyncMode());
        dto.setSyncSchedule(entity.getSyncSchedule());
        dto.setRequestTemplate(entity.getRequestTemplate());
        dto.setAuthType(entity.getAuthType());
        dto.setAuthConfig(entity.getAuthConfig());
        dto.setRetryInterval(entity.getRetryInterval());
        dto.setMaxRetries(entity.getMaxRetries());
        dto.setOnFailure(entity.getOnFailure());
        dto.setSyncTimeParamStart(entity.getSyncTimeParamStart());
        dto.setSyncTimeParamEnd(entity.getSyncTimeParamEnd());
        dto.setSyncTimeFormat(entity.getSyncTimeFormat());
        dto.setFirstSyncDays(entity.getFirstSyncDays());
        dto.setIncrementalSyncHours(entity.getIncrementalSyncHours());
        dto.setLastSyncTime(entity.getLastSyncTime());
        dto.setLastSyncStatus(entity.getLastSyncStatus());
        dto.setLastSyncCount(entity.getLastSyncCount());
        dto.setIsFirstSync(entity.getIsFirstSync());
        dto.setEnabled(entity.getEnabled());
        dto.setDescription(entity.getDescription());
        dto.setSoapAction(entity.getSoapAction());
        dto.setSoapNamespace(entity.getSoapNamespace());
        dto.setSoapParams(entity.getSoapParams());
    }
}