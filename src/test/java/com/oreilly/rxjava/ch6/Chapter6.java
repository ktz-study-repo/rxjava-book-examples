package com.oreilly.rxjava.ch6;

import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static rx.Observable.empty;
import static rx.Observable.just;

@Ignore
public class Chapter6 {

	@Test
	public void testSampling() throws Exception {
		long startTime = System.currentTimeMillis();
		Observable<String> result = Observable
				.interval(7, MILLISECONDS)		// 아무리 7ms마다 이벤트가 발생을 해도,
				.timestamp()
				.sample(1, SECONDS)				// 1초마다 나온다.
				.map(ts -> ts.getTimestampMillis() - startTime + "ms: " + ts.getValue())
				.take(5);
		result.subscribe(System.out::println);

		result.toBlocking().last();
	}

	@Test
	public void testSampling2() throws Exception {
		Observable<String> names =
				just("Mary",
						"Patricia",
						"Linda",		// 1초의 마지막 0.9초에 나오는 것 -> Linda
						"Barbara",	// 2초의 마지막 1.1초에 나오는 것 -> Barbara
										// 3초에 음슴. 한박자 쉬고!
						"Elizabeth",
						"Jennifer",
						"Maria",
						"Susan",		// 4초의 마지막 3.6초에 나오는 것 -> Susan
						"Margaret",
						"Dorothy");	// 5초의 마지막 4.8초에 나오는 것 -> Dorothy

		Observable<Long> absoluteDelayMillis =
				just(0.1,
						0.6,
						0.9,
						1.1,
						3.3,
						3.4,
						3.5,
						3.6,
						4.4,
						4.8)
						.map(d -> (long) (d * 1_000));

		final Observable<String> delayedNames = names
				.zipWith(absoluteDelayMillis,
						(n, d) ->
								just(n)
										.delay(d, MILLISECONDS))
				.flatMap(o -> o);

		Observable<String> sample = delayedNames
				.sample(1, SECONDS);
		sample.subscribe(System.out::println);

		sample.toBlocking().last();
	}

	@Test
	public void throttleFirstExample() throws Exception {
		Observable<String> delayedNames =
				just("Mary",
						"Patricia",
						"Linda",
						"Barbara",
						"Elizabeth",
						"Jennifer",
						"Maria",
						"Susan",
						"Margaret",
						"Dorothy");

		Observable<String> stringObservable = delayedNames
				.throttleFirst(1, SECONDS);		// 일단 기다렸다가 처음껏이 나오면 끝낸다.
		stringObservable.subscribe(System.out::println);

		stringObservable.toBlocking().last();
	}

	@Test
	public void bufferExample() throws Exception {
		Observable
				.range(1, 7)  //1, 2, 3, ... 7
				.buffer(3)					// 3개씩 버퍼를 두고 나온다.
				.subscribe((List<Integer> list) -> {
							System.out.println(list);
						}
				);
	}

	Repository repository = new SomeRepository();

	@Test
	public void buffer10Example() throws Exception {
		Observable<Record> events = Observable.range(1, 100).map(x -> new Record());

		events
				.subscribe(repository::store);
//vs.
		events
				.buffer(10)
				.subscribe(repository::storeAll);

		events.toBlocking().last();

	}

	@Test
	public void BufferSliding() throws Exception {
		Observable<List<Integer>> odd = Observable
				.range(1, 7)
				.buffer(1, 2);	// 각각 1개씩 나오는데, 2개씩 슬라이딩을 한다.
											// [1],2,3,4,5,6,7 -> 1,2,[3],4,5,6,7 -> 1,2,3,4,[5],6,7

		odd.subscribe(System.out::println);
	}

	@Test
	public void flatMapIterableExample() throws Exception {
		Observable<Integer> odd = Observable
				.range(1, 7)
				.buffer(1, 2)
				.flatMapIterable(list -> list);		// Flatten 시김

		odd.subscribe(System.out::println);

		odd.toBlocking().last();
	}

	@Test
	public void sample_155() throws Exception {
		Observable<String> delayedNames =
				just("Mary",
						"Patricia",
						"Linda",
						"Barbara",
						"Elizabeth",
						"Jennifer",
						"Maria",
						"Susan",
						"Margaret",
						"Dorothy");

		delayedNames
				.buffer(1, SECONDS)	// 1초동안 나온 모든 이벤트를 다담음
												// just는 한번에 나옴 -> 다 담음
				.subscribe(System.out::println);
	}

	@Test
	public void Buffer() throws Exception {
		Observable<KeyEvent> keyEvents = empty();

		Observable<List<KeyEvent>> buffer = keyEvents
				.buffer(1, SECONDS);				//buffer는 List로 떨어진다.
		Observable<Integer> eventPerSecond = buffer
				.map(List::size);
	}

// vs.

	@Test
	public void Window() throws Exception {
		Observable<KeyEvent> keyEvents = empty();

		Observable<Observable<KeyEvent>> windows = keyEvents
				.window(1, SECONDS);				// 반면 window는 Observable로 떨어진다.

		Observable<Integer> eventPerSecond = windows
				.flatMap(eventsInSecond -> eventsInSecond.count());
	}

	@Test
	public void merge() throws Exception {
		Observable<Duration> insideBusinessHours = Observable
				.interval(1, SECONDS)
				.filter(x -> isBusinessHour())
				.map(x -> Duration.ofMillis(100));
		Observable<Duration> outsideBusinessHours = Observable
				.interval(5, SECONDS)
				.filter(x -> !isBusinessHour())
				.map(x -> Duration.ofMillis(200));

		Observable<Duration> openings = Observable.merge(
				insideBusinessHours, outsideBusinessHours);
		// http://reactivex.io/documentation/ko/operators/merge.html
		// 두개의 Observable을 이렇게 모아준다.

		Observable<TeleData> upstream = empty();

		Observable<List<TeleData>> samples = upstream
				.buffer(openings);

		Observable<List<TeleData>> samples2 = upstream
				.buffer(
						openings,
						duration -> empty()
								.delay(duration.toMillis(), MILLISECONDS));

	}

	private static final LocalTime BUSINESS_START = LocalTime.of(9, 0);
	private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);

	private boolean isBusinessHour() {
		ZoneId zone = ZoneId.of("Europe/Warsaw");
		ZonedDateTime zdt = ZonedDateTime.now(zone);
		LocalTime localTime = zdt.toLocalTime();
		return !localTime.isBefore(BUSINESS_START)
				&& !localTime.isAfter(BUSINESS_END);
	}

}
