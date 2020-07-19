package org.sid.cinema.dao;


import java.util.List;

import org.sid.cinema.entities.Projection;
import org.sid.cinema.entities.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@RepositoryRestResource
@CrossOrigin("*")
public interface ProjectionRepository extends JpaRepository<Projection, Long>{

	public List<Projection> findBySalle(Salle salle);

}
