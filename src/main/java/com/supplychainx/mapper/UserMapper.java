package com.supplychainx.mapper;

import com.supplychainx.common.dto.UserRequestDTO;
import com.supplychainx.common.dto.UserResponseDTO;
import com.supplychainx.common.entity.User;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    User toEntity(UserRequestDTO dto);
    UserResponseDTO toResponseDTO(User user);
    void updateEntityFromDTO(UserRequestDTO dto, @MappingTarget User user);
}
