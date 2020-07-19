package org.sid.cinema.dao;

import java.util.List;

import org.sid.cinema.entities.Place;
import org.sid.cinema.entities.Salle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@RepositoryRestResource
@CrossOrigin("*")
public interface PlaceRepository extends JpaRepository<Place, Long> {
	public List<Place> findBySalle(Salle s);

	public Page<Place> findBySalle(Salle salle,Pageable pageable);

}
