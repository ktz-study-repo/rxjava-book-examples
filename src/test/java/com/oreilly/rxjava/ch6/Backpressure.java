package com.oreilly.rxjava.ch6;

import com.oreilly.rxjava.util.Sleeper;
import org.apache.commons.dbutils.ResultSetIterator;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

@Ignore
public class Backpressure {

	private static final Logger log = LoggerFactory.getLogger(Backpressure.class);


	private Observable<Dish> dishes() {
		return this.dishes(1_000_000_000);
	}

	private Observable<Dish> dishes(int count) {
		Observable<Dish> dishes = Observable
				.range(1, count)
				.map(Dish::new);
		return dishes;
	}

	@Test
	public void Throttling() throws Exception {
		final Observable<Dish> dishes = dishes(1000);

		dishes
				.observeOn(Schedulers.io())		// 일단 한번에 다 생성함
				.subscribe(x -> {
					System.out.println("Washing: " + x);	// 50ms마다 한번씩 Washing을 한다.
					sleepMillis(50);
				});			// 즉 한번에 다 생산을 해낸다고 생각을 해도 결국은 Throttling해서 다 해낸다.

		Thread.sleep(10000);

	}

	private void sleepMillis(int millis) {
		Sleeper.sleep(Duration.ofMillis(millis));
	}

}
