package com.oreilly.rxjava.ch6;

import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.observables.ConnectableObservable;

import java.math.BigDecimal;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.Observable.defer;

@Ignore
public class Debounce {


	private final TradingPlatform tradingPlatform = new TradingPlatform();

	/**
	 *
	 * 어떤 event후에 아무런 event가 발생하지 않으면 event를 발생한다.
	 * http://reactivex.io/documentation/operators/debounce.html
	 * 예를 들어, debounced가 100ms하라면,
	 * ex1) A event 발생 -> 100ms 후에 -> A event 출력
	 * ex2) A event 발생 -> 50ms 후에 -> B event 발생 -> 100ms 후에 -> B event 출력
	 *
	 */

	@Test
	public void DebounceExample() throws Exception {
		Observable<BigDecimal> prices = tradingPlatform.pricesOf("NFLX");
		Observable<BigDecimal> debounced = prices.debounce(100, MILLISECONDS);

		prices
				.debounce(x -> {
					boolean goodPrice = x.compareTo(BigDecimal.valueOf(150)) > 0;
					return Observable
							.empty()
							.delay(goodPrice? 10 : 100, MILLISECONDS);	// 만약에 goodPrice이면, 10ms후에 event 발생
																		// 만약 goodPrice가 아니면, 100ms후에 event 발생.
				});
	}

	@Test
	public void sample_242() throws Exception {
		Observable
				.interval(99, MILLISECONDS)		// 이렇게 되면 99ms후에는 무조건 이벤트가 되서.
				.debounce(100, MILLISECONDS);	// 암것도 안나옴.
	}
}
