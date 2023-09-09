package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import java.util.*;


/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {


	@Nonnull @Override
	public GameState build(
			Player mrX,
			GameSetup setup,
			ImmutableList<Player> detectives) {
		// return the new updated game state
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);}






	private static Set<SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {

		final var OnlyOneMove = new HashSet<SingleMove>();
		for (int destination : setup.graph.adjacentNodes(source)) {  // find out if destination is occupied by a detective
			boolean occupiedDestination = false;
			for (Player de : detectives) {
				if (de.location() == destination) {
					occupiedDestination = true;
					break;}}
			if (occupiedDestination) {continue;}
			for (Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
				if (player.has(t.requiredTicket()))
					OnlyOneMove.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));}
			if (player.has(Ticket.SECRET)) {
				OnlyOneMove.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));}}
		return Set.copyOf(OnlyOneMove); }


	private static Set<DoubleMove> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source) {
		final var TwoMoves = new HashSet<DoubleMove>();
		if (player.has(Ticket.DOUBLE)){ // check if the player(only mrX) has a double ticket
			for (int destination : setup.graph.adjacentNodes(source)) {  // find out if destination is occupied by a detective
				boolean occupiedDestination = false;
				for(Player de : detectives) {
					if (de.location() == destination){
						occupiedDestination = true;
						break;}}
				if (occupiedDestination){continue;}


				Set<SingleMove> single1 = makeSingleMoves(setup, detectives, player, source);
				for (SingleMove one : single1) {
					Set<SingleMove> single2 = makeSingleMoves(setup,detectives, player, one.destination);
					for (SingleMove two : single2) {
						if (((one.ticket == two.ticket && player.hasAtLeast(one.ticket, 2))) ||
								(one.ticket != two.ticket && player.hasAtLeast(one.ticket,1) && player.hasAtLeast(two.ticket,1))){
							TwoMoves.add(new DoubleMove(player.piece(), source, one.ticket, one.destination, two.ticket, two.destination));}
						if (player.has(Ticket.SECRET)){
							TwoMoves.add(new DoubleMove(player.piece(), source,Ticket.SECRET,one.destination,two.ticket,two.destination));}}}}}

		return ImmutableSet.copyOf(TwoMoves);}


	private static final class MyGameState implements GameState {
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;


		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			if (setup.moves.isEmpty()){ throw new IllegalArgumentException("Error");}

			if (setup.graph.nodes().isEmpty()){ throw new IllegalArgumentException("Error");}

			if (detectives.isEmpty()){ throw new NullPointerException("Error");}

			if (mrX.piece() == null){ throw new NullPointerException("Error");}

			if (remaining.isEmpty()){ throw new IllegalArgumentException("Error");}

			for (Player de : detectives) {
				if (de.has(Ticket.DOUBLE)){ throw new IllegalArgumentException("Error");}
				if (de.has(Ticket.SECRET)) {throw new IllegalArgumentException("Error");}}

			for (int i = 0; i < detectives.size(); i++) {
				for (int j = i + 1; j < detectives.size(); j++) {
					if (detectives.get(i).location() == detectives.get(j).location()){
						throw new IllegalArgumentException("Error");}}}}


		@Nonnull @Override
		public GameSetup getSetup() {
			return setup;}
		//return current game setup


		@Nonnull @Override
		public ImmutableList<LogEntry> getMrXTravelLog() {
			//return Mrx travel log(History)
			return log;}



		@Nonnull @Override
		public ImmutableSet<Piece> getPlayers() {
			Set<Piece> sp = new HashSet<>();
			for (Player de : detectives) {
				sp.add(de.piece());
				sp.add(mrX.piece());}
			//return all the player in the game.

			return ImmutableSet.copyOf(sp);}


		@Nonnull @Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			for (Player de : detectives) {
				if (de.piece() == detective) {
					return Optional.of(de.location());}}
			// return : the location of the given detectives is empty if the detective is not in the game.
			// For all detectives, if Detective#piece == detective, then return the location in an Optional.of();


			return Optional.empty();}


		@Nonnull @Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			for (Player de : detectives) {
				if (de.piece() == piece) {
					return Optional.of(ticket -> de.tickets().get(ticket));}}


			if (mrX.piece() == piece) {
				return Optional.of(ticket -> mrX.tickets().get(ticket));}
			return Optional.empty();}


		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves() {
			Set<Move> allPossibleMoves = new HashSet<>();
			Set<Piece> newWinner = new HashSet<>();
			Set<Piece> detectWin = new HashSet<>();

			for (Player de : detectives){
				detectWin.add(de.piece());} // add the all the detectives in the game.


			for (Player de : detectives){
				if(de.location() == mrX.location()){ // if the detective and mrX is at the same location then mrX is caught.
					newWinner.addAll(detectWin);}}

			if(setup.moves.size() == log.size()){
				if(remaining.contains(mrX.piece())){
					newWinner.add(mrX.piece());}}

			if (setup.moves.size() == log.size()) {
				for (Player  de : detectives){
					if (remaining.contains(de.piece())){
						detectWin.add(de.piece());}}}


			Set<Move> DetectivesAllMove = new HashSet<>();

			for(Player de : detectives){
				DetectivesAllMove.addAll(makeSingleMoves(setup,detectives,de,de.location()));}


			if(DetectivesAllMove.isEmpty()){
				newWinner.add(mrX.piece());}


			if(newWinner.isEmpty()){
				if(remaining.contains(mrX.piece())){
					allPossibleMoves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
					// if the setup round is only one left then obviously at least one move can be made.

					if (setup.moves.size() >= 2){
						allPossibleMoves.addAll(makeDoubleMoves(setup, detectives, mrX, mrX.location()));}

					if(allPossibleMoves.isEmpty()){
						newWinner.addAll(detectWin);}}

				if (!remaining.contains(mrX.piece())){
					for (Player de : detectives){
						if ( remaining.contains(de.piece())){
							allPossibleMoves.addAll(makeSingleMoves(setup,detectives,de,de.location()));}}}}


			winner = ImmutableSet.copyOf(newWinner);
			moves  = ImmutableSet.copyOf(allPossibleMoves);
			return moves;}


		@Nonnull @Override
		public ImmutableSet<Piece> getWinner() {
			getAvailableMoves();
			return winner;}




		@Nonnull @Override
		public GameState advance(Move move) {
			moves = getAvailableMoves();
			if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);

			List<LogEntry> newLogs = new ArrayList<>(log);
			Set<Piece> newRemaining = new HashSet<>(remaining);
			List<Player> newDetectives = new ArrayList<>();

			newRemaining.remove(move.commencedBy());

			int newDestination = move.accept(new Visitor<>() {


				@Override
				public Integer visit(SingleMove singleMove) {
					return singleMove.destination;}

				@Override
				public Integer visit(DoubleMove doubleMove) {
					return doubleMove.destination2;}
			});


			if(move.commencedBy().isMrX()){
				mrX = mrX.use(move.tickets());
				mrX = mrX.at(newDestination); // // mrX hold the ticket and used in the new destination

				newDetectives.addAll(detectives);


				for(Player de : detectives){
					newRemaining.add(de.piece());}

				for (Ticket ticket : move.tickets()){
					if(ticket != Ticket.DOUBLE) {
						if(setup.moves.get(newLogs.size())){
							newLogs.add(LogEntry.reveal(ticket, mrX.location()));}

						else{newLogs.add(LogEntry.hidden(ticket));}}}}


			if (move.commencedBy().isDetective()){
				for(Player de : detectives){
					if (move.commencedBy() == de.piece()){


						de = de.use(move.tickets()); // before played it has one more ticket and after = one less ticket
						de = de.at(newDestination);  // // de hold the ticket and used in the new destination
						newDetectives.add(de);

						mrX = mrX.give(move.tickets());}

					if (move.commencedBy() != de.piece()){
						newDetectives.add(de);}}


				Set<Move> stillRemain = new HashSet<>();

				for (Player de : detectives){
					if(newRemaining.contains(de.piece())){
						stillRemain.addAll(makeSingleMoves(setup,detectives,de,de.location()));}}


				if (stillRemain.isEmpty()){
					newRemaining.add(mrX.piece());}


				if (newRemaining.isEmpty()){
					newRemaining.add(mrX.piece());}}

			return(new MyGameState(setup,ImmutableSet.copyOf(newRemaining), ImmutableList.copyOf(newLogs), mrX, ImmutableList.copyOf(newDetectives)));}}}



//---------------------------------------------------------- All the test passed ------------------------------------------------------------------------//
