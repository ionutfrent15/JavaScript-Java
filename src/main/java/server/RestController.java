package server;

import model.Imagine;
import model.User;
import org.graalvm.compiler.lir.LIRInstruction;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import persistence.ImagineRepo;
import persistence.UserRepository;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@org.springframework.web.bind.annotation.RestController
@Controller
@RequestMapping("/app")
@CrossOrigin(origins = "*")
public class RestController {

    private static SessionFactory sessionFactory;
    private UserRepository userRepository;
    private ImagineRepo imagineRepo;
    private SimpMessageSendingOperations message;

    private int idJucator = 0;
    private Map<Integer, Integer> matches = new HashMap<>();
    private Map<Integer, LocalTime> times = new HashMap<>();
    private Map<Integer, Integer> seconds = new HashMap<>();

    private static void initialize() {
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure()
                .build();
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            StandardServiceRegistryBuilder.destroy( registry );
        }
    }

    @Autowired
    public RestController(SimpMessageSendingOperations message) {
        initialize();

        userRepository = new UserRepository(sessionFactory);
        imagineRepo = new ImagineRepo(sessionFactory);

        this.message = message;
    }

    private int[] suffledIndexes(int n) {
        int[] ar = new int[n];
        for(int i = 0; i < n; i++) {
            ar[i] = i;
        }
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
        return ar;
    }

    @RequestMapping(value = "/imagini", method = RequestMethod.GET)
    public List<Imagine> findAllShuffle() {
        List<Imagine> imgs = imagineRepo.findAll();
        List<Imagine> shuffle = new ArrayList<>();
        int[] indexes = suffledIndexes(imgs.size());
        for(int i = 0; i < indexes.length; i++) {
            shuffle.add(new Imagine(imgs.get(i).getNume(), imgs.get(indexes[i]).getDescriere()));
        }
        return shuffle;
    }


    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public String startJoc() {
        idJucator++;
        times.put(idJucator, LocalTime.now());
        return String.valueOf(idJucator);
    }

    private int numberOfImages() {
        List<Imagine> all = imagineRepo.findAll();
        return all.size();
    }

    @RequestMapping(value = "/check/{id}/{nume}/{descriere}", method = RequestMethod.GET)
    public boolean checkMatch(@PathVariable int id, @PathVariable String nume, @PathVariable String descriere) {
        Imagine imagine = imagineRepo.findOne(nume);
        if(imagine.getDescriere().equals(descriere)) {
            if(matches.containsKey(id)) {
                int count = matches.get(id);
                count++;
                matches.put(id, count);
                if(count == numberOfImages()) {
                    LocalTime timeNow = LocalTime.now();
                    int winningTime = Math.round(ChronoUnit.MILLIS.between(times.get(id), timeNow));
                    if(hallOfFame(winningTime)) {
                        seconds.put(id, winningTime);
                        notifyOneClient(id, new UpdateEvent("hall", winningTime));
                    }
                    else {
                        notifyOneClient(id, new UpdateEvent("final", winningTime));
                    }
                }
            } else {
                matches.put(id, 1);
            }
            return true;
        }
        return false;
    }

    private boolean hallOfFame(@PathVariable double seconds) {
        List<User> all = userRepository.findAll();
        if(all.size() < 3) {
            return true;
        }
        all.sort((o1, o2) -> (int) (o1.getSecunde() - o2.getSecunde()));
        return all.get(2).getSecunde() > seconds;
    }

    @RequestMapping(value = "/hall/{id}/{username}", method = RequestMethod.GET)
    public Dto addHallOfFame(@PathVariable int id, @PathVariable String username) {
        List<User> all = userRepository.findAll();
        User user = new User(username, seconds.get(id));
        if(all.size() < 3) {
            userRepository.save(user);
        } else {
            all.sort((o1, o2) -> (int) (o1.getSecunde() - o2.getSecunde()));
            User old = all.get(2);
            userRepository.delete(old);
            userRepository.save(user);
        }
        all = userRepository.findAll();
        all.sort((o1, o2) -> (int) (o1.getSecunde() - o2.getSecunde()));

        return new Dto(user, all);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addImagine(@RequestParam String username, @RequestParam String image, @RequestParam String descr) {
        Imagine imagine = new Imagine(image, descr);
        imagineRepo.save(imagine);
        return "SAVED";
    }

    public void notifyOneClient(int id, UpdateEvent updateEvent) {
        message.convertAndSend("/update/" + id, updateEvent);
    }

//    public void notifyAllClients(UpdateEvent updateEvent) {
//        message.convertAndSend("/update", updateEvent);
//    }
}
