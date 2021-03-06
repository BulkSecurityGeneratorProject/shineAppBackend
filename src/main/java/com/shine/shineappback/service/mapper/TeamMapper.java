package com.shine.shineappback.service.mapper;

import com.shine.shineappback.domain.*;
import com.shine.shineappback.service.dto.TeamDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Team and its DTO TeamDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeamMapper extends EntityMapper<TeamDTO, Team> {

    @Mapping(source = "supervisor.id", target = "supervisorId")
    @Mapping(source = "supervisor.login", target = "supervisorLogin")
    TeamDTO toDto(Team team);

    @Mapping(source = "supervisorId", target = "supervisor")
    Team toEntity(TeamDTO teamDTO);

    default Team fromId(Long id) {
        if (id == null) {
            return null;
        }
        Team team = new Team();
        team.setId(id);
        return team;
    }
}
