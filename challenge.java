import java.util.UUID;

public class BTG {

    public static void main(String[] args) {


    }

    public static enum HashGameEnum {
        PEDRA, PAPEL, TESOURA;
    }

    public static class PlayerForm {

        private String email;

        private HashGameEnum choice;

        public PlayerForm() {
        }

        public String getEmail() {
            return email;
        }

        public HashGameEnum getChoice() {
            return choice;
        }
    }

    @Entity
    public static class Game {

        private UUID id;
        private String winPlayer;
        private String loserPlayer;
        private HashGameEnum winChoice;


        public Game() {
        }

        public Game(UUID id, String winPlayer, String loserPlayer, HashGameEnum winChoice) {
            this.id = id;
            this.winPlayer = winPlayer;
            this.loserPlayer = loserPlayer;
            this.winChoice = winChoice;
        }

        public UUID getId() {
            return id;
        }

        public String getWinPlayer() {
            return winPlayer;
        }

        public void setWinPlayer(String winPlayer) {
            this.winPlayer = winPlayer;
        }

        public String getLoserPlayer() {
            return loserPlayer;
        }

        public void setLoserPlayer(String loserPlayer) {
            this.loserPlayer = loserPlayer;
        }

        public HashGameEnum getWinChoice() {
            return winChoice;
        }

        public void setWinChoice(HashGameEnum winChoice) {
            this.winChoice = winChoice;
        }
    }

    public static class GameForm {
        private PlayerForm playerOne;
        private PlayerForm playerTwo;

        public GameForm() {
        }

        public PlayerForm getPlayerOne() {
            return playerOne;
        }

        public PlayerForm getPlayerTwo() {
            return playerTwo;
        }
    }

    public static class GameDTO {

        private String winPlayer;
        private String loserPlayer;
        private HashGameEnum winChoice;


        public GameDTO() {
        }

        public String getWinPlayer() {
            return winPlayer;
        }

        public void setWinPlayer(String winPlayer) {
            this.winPlayer = winPlayer;
        }

        public String getLoserPlayer() {
            return loserPlayer;
        }

        public void setLoserPlayer(String loserPlayer) {
            this.loserPlayer = loserPlayer;
        }

        public HashGameEnum getWinChoice() {
            return winChoice;
        }

        public void setWinChoice(HashGameEnum winChoice) {
            this.winChoice = winChoice;
        }
    }
    
    private interface Mapper<M, D> { // Poderia ser utilizado com inversão de dependência
        D toDTO(M m);
    }
    private class GameMapper implements Mapper<Game, GameDTO> {
        
        @Override
        public GameDTO toDTO(Game game) {
            GameDTO dto = new GameDTO();
            dto.setWinChoice(game.winChoice);
            dto.setWinPlayer(game.winPlayer);
            dto.setLoserPlayer(game.loserPlayer);
            return dto;
        }
    }

    private interface GameRepository extends JpaRepository<Game, UUID> {

    }

    @Service
    private class GameService {

        private final GameRepository gameRepository;
        private final GameMapper gameMapper;

        public GameService(GameRepository gameRepository, GameMapper gameMapper) { // Falta realizar a inversão de dependências
            this.gameRepository = gameRepository;
            this.gameMapper = gameMapper;
        }

        public GameDTO registerGame(GameForm form) {
            Game game = doTheGame(form);
            gameRepository.save(game);
            return gameMapper.toDTO(game);
        }

        private Game doTheGame(GameForm form) {
            HashGameEnum playerOneChoice = form.playerOne.choice;
            HashGameEnum playerTwoChoice = form.playerTwo.choice;

            String playerOneEmail = form.playerOne.email;
            String playerTwoEmail = form.playerTwo.email;

            Game game;

            // PEDRA > TESOURA > PAPEL > PEDRA...
            if (playerOneChoice.equals(playerTwoChoice)) {
                throw new RuntimeException("Choices cannot be the same. Try again, please."); // BAD_REQUEST - Tratamento na api seria feito no handling
            } else if (playerOneChoice == HashGameEnum.PAPEL && playerTwoChoice == HashGameEnum.PEDRA) {
                game = prepareGameByWinnerAndLoser(playerOneEmail, playerTwoEmail, playerOneChoice);
            } else if (playerOneChoice == HashGameEnum.PEDRA && playerTwoChoice == HashGameEnum.TESOURA) {
                game = prepareGameByWinnerAndLoser(playerOneEmail, playerTwoEmail, playerOneChoice);
            } else if (playerOneChoice == HashGameEnum.TESOURA && playerTwoChoice == HashGameEnum.PAPEL) {
                game = prepareGameByWinnerAndLoser(playerOneEmail, playerTwoEmail, playerOneChoice);
            } else {
                game = prepareGameByWinnerAndLoser(playerTwoEmail, playerOneEmail, playerTwoChoice);
            }
            return game;
        }

        private Game prepareGameByWinnerAndLoser(String winPlayer, String loserPlayer, HashGameEnum winChoice) {
            return new Game(UUID.randomUUID(), winPlayer, loserPlayer, winChoice);
        }
    }

    @RestController
    public class GameController {
        private final GameService gameService;

        public GameController(GameService gameService) {
            this.gameService = gameService;
        }

        @PostMapping("/games")
        public ResponseEntity<GameDTO> registerGame(@RequesBody GameForm form) {
            GameDTO game = gameService.registerGame(form);
            return ResponseEntity.created().build(game);
        }
    }
}
