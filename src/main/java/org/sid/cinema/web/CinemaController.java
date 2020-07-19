package org.sid.cinema.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.sid.cinema.dao.CinemaRepository;
import org.sid.cinema.dao.FilmRepository;
import org.sid.cinema.dao.PlaceRepository;
import org.sid.cinema.dao.ProjectionRepository;
import org.sid.cinema.dao.SalleRepository;
import org.sid.cinema.dao.SeanceRepository;
import org.sid.cinema.dao.TicketRepository;
import org.sid.cinema.dao.VilleRepository;
import org.sid.cinema.entities.Cinema;
import org.sid.cinema.entities.Film;
import org.sid.cinema.entities.Place;
import org.sid.cinema.entities.Projection;
import org.sid.cinema.entities.Salle;
import org.sid.cinema.entities.Seance;
import org.sid.cinema.entities.Ticket;
import org.sid.cinema.entities.Ville;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CinemaController {
	// chemin d upload
	private final String UPLOAD_DIR = System.getProperty("user.home") + "/cinema/images/";

	@Autowired
	private CinemaRepository cinemaRepository;
	@Autowired
	private FilmRepository filmRepository;
	@Autowired
	private VilleRepository villeRepository;
	@Autowired
	private SalleRepository salleRepository;
	@Autowired
	private SeanceRepository seanceRepository;
	@Autowired
	private ProjectionRepository projectionRepository;
	@Autowired
	private PlaceRepository placeRepository;
	@Autowired
	private TicketRepository ticketRepository;

	// page principale
	@GetMapping(path = "/index")
	public String index() {
		return "index";
	}

	/****
	 * film***film***film***film***film***film***film***film***film***film***film
	 ****/
	@GetMapping(path = "/film")
	public String films(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "4") int size,
			@RequestParam(name = "name", defaultValue = "") String name) {
		Page<Film> pageFilms = filmRepository.findByTitreContains(name, PageRequest.of(page, size));
		model.addAttribute("pageFilms", pageFilms);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);
		model.addAttribute("pages", new int[pageFilms.getTotalPages()]);
		return "film";
	}

	@GetMapping(path = "/deleteFilm")
	public String deleteFilm(Long id, String name, String page, String size) {
		filmRepository.deleteById(id);
		return "redirect:/film?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/updateFilm")
	public String updateFilm(@RequestParam(name = "nf") String nf, @RequestParam(name = "rea") String rea,
			@RequestParam(name = "ds") String ds, @RequestParam(name = "desc") String desc,
			@RequestParam(name = "page") int page, @RequestParam(name = "size") int size,
			@RequestParam(name = "name") String name, @RequestParam(name = "id") Long id) {

		Date d;
		Optional<Film> f = filmRepository.findById(id);
		f.get().setTitre(nf);
		f.get().setRealisateur(rea);
		f.get().setDescription(desc);
		try {
			d = new SimpleDateFormat("yyyy-MM-dd").parse(ds);
			f.get().setDateSortie(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		filmRepository.save(f.get());

		return "redirect:/film?page=" + page + "&name=" + name + "&size=" + size;
	}

	@PostMapping(path = "/addFilm")
	public String addFilm(@RequestParam(name = "tf") String tf, @RequestParam(name = "ds") String ds,
			@RequestParam(name = "rf") String rf, @RequestParam(name = "desc") String desc,
			@RequestParam(name = "duree", defaultValue = "0") int duree, @RequestParam(name = "page") int page,
			@RequestParam(name = "name") String name, @RequestParam(name = "size") int size,
			@RequestParam("file") MultipartFile file

	) throws ParseException {

		String fileName = tf.replaceAll(" ", "") + ".jpg";

		try {
			Path path = Paths.get(UPLOAD_DIR + fileName);
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Date d = new SimpleDateFormat("yyyy-MM-dd").parse(ds);
		Film film = new Film();
		film.setDescription(desc);
		film.setDuree(duree);
		film.setTitre(tf);
		film.setRealisateur(rf);
		film.setDateSortie(d);
		film.setPhoto(tf.replaceAll(" ", "") + ".jpg");

		filmRepository.save(film);

		return "redirect:/film?page=" + page + "&name=" + name + "&size=" + size;
	}

	/**** salle***salle***salle***salle***salle***salle***salle ****/
	@GetMapping(path = "/salle")
	public String salle(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "6") int size,
			@RequestParam(name = "name", defaultValue = "") String name,@RequestParam(name = "idc", defaultValue = "0") Long idc) {

		Cinema c=cinemaRepository.getOne(idc);
		Page<Salle> pageSalles=null;
		if(idc==0) {
			pageSalles=salleRepository.findByNameContains(name, PageRequest.of(page, size));
		}
		else
		{
			pageSalles=salleRepository.findByCinema(c,PageRequest.of(page, size));
		}
		
		
		List<Cinema> cinemas = cinemaRepository.findAll();
		model.addAttribute("pageSalles", pageSalles);
		model.addAttribute("cinemas", cinemas);
		model.addAttribute("idc", idc);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);
		model.addAttribute("pages", new int[pageSalles.getTotalPages()]);
		return "salle";
	}

	@GetMapping(path = "/deleteSalle")
	public String deleteSalle(Long id, String name, String page, String size) {

		Salle s = salleRepository.findById(id).get();
		
		placeRepository.findBySalle(s).forEach(place -> {
			ticketRepository.findByPlace(place).forEach(ticket-> ticketRepository.deleteById(ticket.getId()));
			placeRepository.deleteById(place.getId());
		});
		
		
		projectionRepository.findBySalle(s).forEach(proj -> projectionRepository.deleteById(proj.getId()));
		Cinema c = s.getCinema();
		c.setNombreSalles(c.getNombreSalles() - 1);
		cinemaRepository.save(c);

		salleRepository.deleteById(id);
		return "redirect:/salle?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/updateSalle")
	public String updateSalle(@RequestParam(name = "ns") String ns,
			@RequestParam(name = "nbr", defaultValue = "0") int nbr, @RequestParam(name = "nc") Long nc,
			@RequestParam(name = "page") int page, @RequestParam(name = "size") int size,
			@RequestParam(name = "name") String name, @RequestParam(name = "id") Long id) {
		Optional<Salle> s = salleRepository.findById(id);
		Optional<Cinema> c = cinemaRepository.findById(nc);
		s.get().setName(ns);
		s.get().setNombrePlace(nbr);
		s.get().setCinema(c.get());
		salleRepository.save(s.get());

		return "redirect:/salle?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/addSalle")
	public String addSalle(@RequestParam(name = "ns") String ns, @RequestParam(name = "nc") Long nc,
			@RequestParam(name = "nbr", defaultValue = "0") int nbr, @RequestParam(name = "page") int page,
			@RequestParam(name = "name") String name, @RequestParam(name = "size") int size) {
		Cinema c = cinemaRepository.findById(nc).get();
		Salle newsalle = new Salle();
		newsalle.setName(ns);
		newsalle.setNombrePlace(nbr);
		newsalle.setCinema(c);
		c.setNombreSalles(c.getNombreSalles() + 1);
		cinemaRepository.save(c);
		salleRepository.save(newsalle);
		for (int i = 0; i < newsalle.getNombrePlace(); i++) {
			Place place = new Place();
			place.setNumero(i + 1);
			place.setSalle(newsalle);
			placeRepository.save(place);
			
			
		}

		return "redirect:/salle?page=" + page + "&name=" + name + "&size=" + size;
	}
	/****place***place***place***place***place***place***place****/
	@GetMapping(path = "/place")
	public String places(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "8") int size,@RequestParam(name = "ids", defaultValue = "0") Long ids) {
		
		Salle salle=salleRepository.getOne(ids);
		Page<Place> placePages=null;
		if(ids==0) {
			placePages=placeRepository.findAll(PageRequest.of(page, size));
		}
		else
		{
			placePages=placeRepository.findBySalle(salle,PageRequest.of(page, size));
		}
		
		model.addAttribute("placePages", placePages);
		model.addAttribute("ids", ids);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("pages", new int[placePages.getTotalPages()]);
		return "place";
	}
	/****ticket***ticket***ticket***ticket***ticket***ticket***ticket***ticket****/
	@GetMapping(path = "/ticket")
	public String tickets() {
		return "ticket";
	}
	/****seance***seance***seance***seance***seance***seance***seance****/
	@GetMapping(path = "/seance")
	public String seance(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size) {

		Page<Seance> pageSeances = seanceRepository.findAll(PageRequest.of(page, size));

		model.addAttribute("pageSeances", pageSeances);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("pages", new int[pageSeances.getTotalPages()]);
		return "seance";
	}

	@GetMapping(path = "/deleteSeance")
	public String deleteSeance(Long id, String hd, String page, String size) {
		seanceRepository.deleteById(id);
		return "redirect:/seance?page=" + page + "&hd=" + hd + "&size=" + size;
	}

	@GetMapping(path = "/updateSeance")
	public String updateSeances(@RequestParam(name = "hdd") Time hdd, @RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size, @RequestParam(name = "hd") String hd,
			@RequestParam(name = "id") Long id) {

		Optional<Seance> s = seanceRepository.findById(id);

		s.get().setHeureDebut(hdd);
		seanceRepository.save(s.get());

		return "redirect:/seance?page=" + page + "&hd=" + hd + "&size=" + size;
	}

	@GetMapping(path = "/addSeance")
	public String addSeance(@RequestParam(name = "hdd") Time hdd, @RequestParam(name = "page") int page,
			@RequestParam(name = "hd") String hd, @RequestParam(name = "size") int size) throws ParseException {

		/*
		 * DateFormat dateFormat = new SimpleDateFormat("hh:mm"); Date d =
		 * dateFormat.parse(hdd);
		 */
		Seance newseance = new Seance();
		newseance.setHeureDebut(hdd);
		seanceRepository.save(newseance);

		return "redirect:/seance?page=" + page + "&hd=" + hd + "&size=" + size;
	}
	/****cinema***cinema***cinema***cinema***cinema***cinema***cinema***cinema****/
	@GetMapping(path = "/cinema")
	public String cinema(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size,
			@RequestParam(name = "name", defaultValue = "") String name) {

		Page<Cinema> pageCinemas = cinemaRepository.findByNameContains(name, PageRequest.of(page, size));
		List<Ville> villes = villeRepository.findAll();
		model.addAttribute("pageCinemas", pageCinemas);
		model.addAttribute("villes", villes);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);
		model.addAttribute("pages", new int[pageCinemas.getTotalPages()]);
		return "cinema";
	}

	@GetMapping(path = "/deleteCinema")
	public String deleteCinema(Long id, String name, String page, String size) {

		salleRepository.findByCinema(cinemaRepository.getOne(id)).forEach(salle -> {

			placeRepository.findBySalle(salle).forEach(place ->{
				ticketRepository.findByPlace(place).forEach(ticket-> ticketRepository.deleteById(ticket.getId()));
				placeRepository.deleteById(place.getId());
				
			});
			
			projectionRepository.findBySalle(salle).forEach(proj -> projectionRepository.deleteById(proj.getId()));
			
			salleRepository.deleteById(salle.getId());
		});

		cinemaRepository.deleteById(id);
		return "redirect:/cinema?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/updateCinema")
	public String updateCinema(@RequestParam(name = "nc") String nc,
			@RequestParam(name = "nbr", defaultValue = "0") int nbr, @RequestParam(name = "nv") Long nv,
			@RequestParam(name = "page") int page, @RequestParam(name = "size") int size,
			@RequestParam(name = "name") String name, @RequestParam(name = "id") Long id) {
		Optional<Cinema> c = cinemaRepository.findById(id);
		Optional<Ville> v = villeRepository.findById(nv);
		c.get().setName(nc);
		c.get().setNombreSalles(nbr);
		c.get().setVille(v.get());
		cinemaRepository.save(c.get());

		return "redirect:/cinema?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/addCinema")
	public String addCinema(@RequestParam(name = "nc") String nc, @RequestParam(name = "nv") Long nv,
			@RequestParam(name = "nbr", defaultValue = "0") int nbr, @RequestParam(name = "page") int page,
			@RequestParam(name = "name") String name, @RequestParam(name = "size") int size) {
		Optional<Ville> v = villeRepository.findById(nv);
		Cinema newcinema = new Cinema();
		newcinema.setName(nc);
		newcinema.setNombreSalles(nbr);
		newcinema.setVille(v.get());
		cinemaRepository.save(newcinema);

		for (int i = 0; i < newcinema.getNombreSalles(); i++) {
			Salle salle = new Salle();
			salle.setName("Salle " + (i + 1));
			salle.setCinema(newcinema);
			salle.setNombrePlace(15 + (int) (Math.random() * 20));
			salleRepository.save(salle);
			for (int j = 0; j < salle.getNombrePlace(); j++) {
				Place place = new Place();
				place.setNumero(j + 1);
				place.setSalle(salle);
				placeRepository.save(place);
				
				
			}
			
			
			
		}

		return "redirect:/cinema?page=" + page + "&name=" + name + "&size=" + size;
	}
	/****ville***ville***ville***ville***ville***ville***ville****/
	@GetMapping(path = "/ville")
	public String ville(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size,
			@RequestParam(name = "name", defaultValue = "") String name) {

		Page<Ville> pageVilles = villeRepository.findByNameContains(name, PageRequest.of(page, size));

		model.addAttribute("pageVilles", pageVilles);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);
		model.addAttribute("pages", new int[pageVilles.getTotalPages()]);
		return "ville";
	}

	@GetMapping(path = "/deleteVille")
	public String deleteVille(Long id, String name, String page, String size) {
		villeRepository.deleteById(id);
		return "redirect:/ville?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/updateVille")
	public String updateVille(@RequestParam(name = "nv") String nv, @RequestParam(name = "page") int page,
			@RequestParam(name = "size") int size, @RequestParam(name = "name") String name,
			@RequestParam(name = "id") Long id) {

		Optional<Ville> v = villeRepository.findById(id);

		v.get().setName(nv);
		villeRepository.save(v.get());

		return "redirect:/ville?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/addVille")
	public String addVille(@RequestParam(name = "nv") String nv, @RequestParam(name = "page") int page,
			@RequestParam(name = "name") String name, @RequestParam(name = "size") int size) {

		Ville newville = new Ville();
		newville.setName(nv);
		villeRepository.save(newville);

		return "redirect:/ville?page=" + page + "&name=" + name + "&size=" + size;
	}
	/****projection***projection***projection***projection***projection***projection****/
	@GetMapping(path = "/projection")
	public String projections(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "8") int size,
			@RequestParam(name = "name", defaultValue = "") String name) {
		Page<Projection> pageProjections = projectionRepository.findAll(PageRequest.of(page, size));
		List<Film> films = filmRepository.findAll();
		List<Seance> seances = seanceRepository.findAll();
		List<Ville> villes = villeRepository.findAll();
		List<Salle> salles = salleRepository.findAll();
		List<Cinema> cinemas = cinemaRepository.findAll();

		model.addAttribute("pageProjections", pageProjections);
		model.addAttribute("seances", seances);
		model.addAttribute("films", films);
		model.addAttribute("villes", villes);
		model.addAttribute("salles", salles);
		model.addAttribute("cinemas", cinemas);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);
		model.addAttribute("pages", new int[pageProjections.getTotalPages()]);
		return "projection";
	}

	@GetMapping(path = "/deleteProjection")
	public String deleteProjection(Long id, String name, String page, String size) {
		
		ticketRepository.findByProjection(projectionRepository.getOne(id)).forEach(ticket->ticketRepository.deleteById(ticket.getId()));
		projectionRepository.deleteById(id);
		return "redirect:/projection?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/updateProjection")
	public String updateProjection(
			@RequestParam(name = "nf") Long nf, @RequestParam(name = "dp") String dp,
			@RequestParam(name = "seance") Long seance, @RequestParam(name = "prix") double prix,
			@RequestParam(name = "page") int page, @RequestParam(name = "size") int size,
			@RequestParam(name = "name") String name, @RequestParam(name = "id") Long id) {

		Projection projection = projectionRepository.findById(id).get();
		Film film = filmRepository.findById(nf).get();
		Seance s = seanceRepository.findById(seance).get();
		Date d;
		try {
			d = new SimpleDateFormat("yyyy-MM-dd").parse(dp);
			projection.setDateProjection(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		projection.setFilm(film);
		projection.setSeance(s);
		projection.setPrix(prix);
		projectionRepository.save(projection);

		return "redirect:/projection?page=" + page + "&name=" + name + "&size=" + size;
	}

	@GetMapping(path = "/addProjection")
	public String addProjection(@RequestParam(name = "nf") Long nf, @RequestParam(name = "dp") String dp,
			@RequestParam(name = "seance") Long seance, @RequestParam(name = "salle") Long salle,
			@RequestParam(name = "prix") double prix, @RequestParam(name = "page") int page,
			@RequestParam(name = "name") String name, @RequestParam(name = "size") int size

	) {
		Projection projection = new Projection();
		Film film = filmRepository.findById(nf).get();
		Seance s = seanceRepository.findById(seance).get();
		Salle sa = salleRepository.findById(salle).get();
		Date d;
		try {
			d = new SimpleDateFormat("yyyy-MM-dd").parse(dp);
			projection.setDateProjection(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		projection.setFilm(film);
		projection.setSeance(s);
		projection.setPrix(prix);
		projection.setSalle(sa);
		projectionRepository.save(projection);
		sa.getProjections().add(projection);
		salleRepository.save(sa);
		placeRepository.findBySalle(sa).forEach(place -> {
			Ticket ticket=new Ticket();
			ticket.setPlace(place);
			ticket.setReserve(false);
			ticket.setPrix(prix);
			ticket.setProjection(projection);
			ticketRepository.save(ticket);
		});
		
		return "redirect:/projection?page=" + page + "&name=" + name + "&size=" + size;
	}

}
