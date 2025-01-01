package org.example;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class EventRelationProcessor extends KeyedProcessFunction<Long, DetectionEvent, EventAlert> {

    private static final long serialVersionUID = 1L;

    private static final double CONFIDENCE_THRESHOLD = 0.2;
    private static final long TIME_WINDOW = 60 * 1000; // 1 minute

    private transient ValueState<DetectionEvent> previousEventState;
    private transient ValueState<Long> timerState;

    @Override
    public void open(OpenContext openContext) {
        ValueStateDescriptor<DetectionEvent> eventDescriptor = new ValueStateDescriptor<>(
                "previous-event",
                Types.POJO(DetectionEvent.class));
        previousEventState = getRuntimeContext().getState(eventDescriptor);

        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
                "timer-state",
                Types.LONG);
        timerState = getRuntimeContext().getState(timerDescriptor);
    }

    @Override
    public void processElement(
            DetectionEvent currentEvent,
            Context ctx,
            Collector<EventAlert> out) throws Exception {

        DetectionEvent previousEvent = previousEventState.value();

        if (previousEvent != null) {

            if (currentEvent.isDefective && previousEvent.isDefective) {
                boolean sameDefectType = currentEvent.defectType.equals(previousEvent.defectType);
                boolean confidenceSpike = Math.abs(currentEvent.confidenceScore - previousEvent.confidenceScore) > CONFIDENCE_THRESHOLD;

                if (sameDefectType && confidenceSpike) {
                    EventAlert alert = new EventAlert();
                    alert.factoryId = currentEvent.factoryId;
                    alert.alertMessage = "Defect type repeated with significant confidence change.";
                    alert.timestamp = ctx.timerService().currentProcessingTime();
                    out.collect(alert);

                    // Clean up state
                    cleanUp(ctx);
                }
            }
        }

        previousEventState.update(currentEvent);

        long timer = ctx.timerService().currentProcessingTime() + TIME_WINDOW;
        ctx.timerService().registerProcessingTimeTimer(timer);
        timerState.update(timer);
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<EventAlert> out) {
        // Clean up all states after the timer expires
        timerState.clear();
        previousEventState.clear();
    }

    private void cleanUp(Context ctx) throws Exception {

        Long timer = timerState.value();
        if (timer != null) {
            ctx.timerService().deleteProcessingTimeTimer(timer);
        }

        timerState.clear();
        previousEventState.clear();
    }
}
