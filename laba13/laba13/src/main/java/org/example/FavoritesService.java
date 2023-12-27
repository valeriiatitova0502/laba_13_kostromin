package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FavoritesService {

    private final Set<Song> favorites;

    @Autowired
    private Playlist playlist;

    @Autowired
    private MileService mileService;

    @Autowired
    private SongService songService;

    @Autowired
    private SongRequest songRequest;

    public FavoritesService() {
        this.favorites = new HashSet<>();
    }

    @PostConstruct
    public void init() {
    }

    public void printUpdatedFavorites() {
        System.out.println("\nОбновленное избранное перед удалением:");
        printFavorites();
    }

    public boolean addToFavorites(Song song) {
        String formattedDate = getCurrentTime();
        if (!favorites.add(song)) {
            System.out.println("Песня '" + song.getName() + "' уже есть в избранном.");
            return false;
        } else {
            mileService.sendFavoritesToEmail(song.getName(), Action.ADDED);
            return true;
        }
    }

    public boolean removeFromFavorites(Song song) {
        if (favorites.remove(song)) {
            mileService.sendFavoritesToEmail(song.getName(), Action.REMOVED);
            return true; // Успешное удаление
        } else {
            mileService.sendFavoritesToEmail(song.getName(), Action.ADDED);
            return false; // Песня не найдена, операция не выполнена
        }
    }

    public void addSongsToFavoriteByName(String... songNames) {
        for (String songName : songNames) {
            Song song = songRequest.findByName(songName);
            if (song != null) {
                addToFavorites(song);
            }
        }
    }

    public void removeSongFromFavoritesByName(String songName) {
        Song song = songRequest.findByName(songName);
        if (song != null) {
            removeFromFavorites(song);
        } else {
            System.out.println("Песня '" + songName + "' не найдена.");
        }
    }

    public Set<Song> getFavorites() {
        return new HashSet<>(favorites);
    }

    public void printFavorites() {
        System.out.println("\nИзбранное:");
        for (Song song : favorites) {
            System.out.println(song.printInfo());
        }
    }

    public void addToPlaylistFromFavorites() {
        Set<Song> favoriteSongs = getFavorites();
        addToPlaylist(new ArrayList<>(favoriteSongs));
    }

    private void addToPlaylist(List<Song> allSongs) {
        playlist.setSongs(allSongs);
        updatePlaylist();
    }

    public void updatePlaylist() {
        List<Song> allSongs = songService.getAllSongs();
        playlist.setSongs(allSongs);
        playlist.displayPlaylist();
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }
}
