package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;


/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override
	public Model build(GameSetup setup,
					   Player mrX,
					   ImmutableList<Player> detectives) {

		return new Model() {

			private Board.GameState gameState = new MyGameStateFactory().build(setup, mrX, detectives);
			private ImmutableSet<Observer> observers = ImmutableSet.of();


			@Nonnull @Override
			public Board getCurrentBoard() {

				return gameState;}

			@Override
			public void registerObserver(@Nonnull Observer observer) {
				ArrayList<Observer> ob = new ArrayList<>(observers);

				if (observer == null) {throw new NullPointerException();}
				if (observers.contains(observer)) {throw new IllegalArgumentException();}
				else {ob.add(observer);}
				observers = ImmutableSet.copyOf(ob);}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {
				ArrayList<Observer> ob = new ArrayList<>(observers);

				if (observer == null) {throw new NullPointerException();}
				if (!ob.contains(observer)) {throw new IllegalArgumentException();}
				else {ob.remove(observer);}
				observers = ImmutableSet.copyOf(ob);}

			@Nonnull @Override
			public ImmutableSet<Observer> getObservers() {
				return observers;}


			@Override
			public void chooseMove(@Nonnull Move move) {
				gameState = gameState.advance(move);
				for (Observer ob : observers) {
					if (gameState.getWinner().isEmpty()) {
						ob.onModelChanged(gameState, Observer.Event.MOVE_MADE);}
					else {
						ob.onModelChanged(gameState, Observer.Event.GAME_OVER);}}}};}}