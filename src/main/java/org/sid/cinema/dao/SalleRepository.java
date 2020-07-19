package org.sid.cinema.dao;

import java.util.List;

import org.sid.cinema.entities.Cinema;
import org.sid.cinema.entities.Place;
import org.sid.cinema.entities.Salle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;


@RepositoryRestResource
@CrossOrigin("*")
public interface SalleRepository extends JpaRepository<Salle, Long>{


	public Page<Salle> findByNameContains(String name, Pageable pageable);

	public List<Salle> findByCinema(Cinema c);
	public Page<Salle> findByCinema(Cinema c,Pageable pageable);
	
	
}
