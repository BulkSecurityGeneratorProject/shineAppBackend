package com.shine.shineappback.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.shine.shineappback.service.TeamService;
import com.shine.shineappback.web.rest.errors.BadRequestAlertException;
import com.shine.shineappback.web.rest.util.HeaderUtil;
import com.shine.shineappback.web.rest.util.PaginationUtil;
import com.shine.shineappback.service.dto.TeamDTO;
import com.shine.shineappback.service.dto.TeamCriteria;
import com.shine.shineappback.service.TeamQueryService;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Team.
 */
@RestController
@RequestMapping("/api")
public class TeamResource {

    private final Logger log = LoggerFactory.getLogger(TeamResource.class);

    private static final String ENTITY_NAME = "team";

    private final TeamService teamService;

    private final TeamQueryService teamQueryService;

    public TeamResource(TeamService teamService, TeamQueryService teamQueryService) {
        this.teamService = teamService;
        this.teamQueryService = teamQueryService;
    }

    /**
     * POST  /teams : Create a new team.
     *
     * @param teamDTO the teamDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new teamDTO, or with status 400 (Bad Request) if the team has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/teams")
    @Timed
    public ResponseEntity<TeamDTO> createTeam(@Valid @RequestBody TeamDTO teamDTO) throws URISyntaxException {
        log.debug("REST request to save Team : {}", teamDTO);
        if (teamDTO.getId() != null) {
            throw new BadRequestAlertException("A new team cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TeamDTO result = teamService.save(teamDTO);
        return ResponseEntity.created(new URI("/api/teams/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /teams : Updates an existing team.
     *
     * @param teamDTO the teamDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated teamDTO,
     * or with status 400 (Bad Request) if the teamDTO is not valid,
     * or with status 500 (Internal Server Error) if the teamDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/teams")
    @Timed
    public ResponseEntity<TeamDTO> updateTeam(@Valid @RequestBody TeamDTO teamDTO) throws URISyntaxException {
        log.debug("REST request to update Team : {}", teamDTO);
        if (teamDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        TeamDTO result = teamService.save(teamDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, teamDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /teams : get all the teams.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of teams in body
     */
    @GetMapping("/teams")
    @Timed
    public ResponseEntity<List<TeamDTO>> getAllTeams(TeamCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Teams by criteria: {}", criteria);
        Page<TeamDTO> page = teamQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/teams");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /teams/:id : get the "id" team.
     *
     * @param id the id of the teamDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the teamDTO, or with status 404 (Not Found)
     */
    @GetMapping("/teams/{id}")
    @Timed
    public ResponseEntity<TeamDTO> getTeam(@PathVariable Long id) {
        log.debug("REST request to get Team : {}", id);
        Optional<TeamDTO> teamDTO = teamService.findOne(id);
        return ResponseUtil.wrapOrNotFound(teamDTO);
    }

    /**
     * DELETE  /teams/:id : delete the "id" team.
     *
     * @param id the id of the teamDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/teams/{id}")
    @Timed
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        log.debug("REST request to delete Team : {}", id);
        teamService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
