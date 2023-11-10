package com.couchbase.mobileclient.triggers;

import com.couchbase.lite.ReplicatorActivityLevel;
import com.couchbase.lite.ReplicatorChange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.couchbase.lite.ReplicatorActivityLevel.*;

public class StatusCountAndTimeTrigger implements Trigger<ReplicatorChange> {

    final StatusHolder holder = new StatusHolder();
    final CountAndTimeTrigger trigger;

    public StatusCountAndTimeTrigger() {
        this(CountAndTimeTrigger.TriggerConfig.builder().build());
    }

    public StatusCountAndTimeTrigger(CountAndTimeTrigger.TriggerConfig defaultConfig) {
        trigger = new CountAndTimeTrigger(defaultConfig);

    }

    @Override
    public boolean onNextElement(ReplicatorChange changes) {
        ReplicatorActivityLevel currentStatus = changes.getStatus().getActivityLevel();
        long completed = changes.getStatus().getProgress().getCompleted();
        long totalTarget = changes.getStatus().getProgress().getTotal();
        long delta = completed - trigger.getCounter().get();
        return holder.compareAndSet(currentStatus) || trigger.onNextElement(delta) /*|| completed == totalTarget && delta == 0*/;
    }

    @Override
    public void reset() {
        //TODO
        trigger.reset();

    }

    @Override
    public boolean onNextElement() {
        return false;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class StatusHolder{
        @Builder.Default
        ReplicatorActivityLevel lastStatus = OFFLINE;

        public boolean isStatusChanged(ReplicatorActivityLevel current) {
            return  !lastStatus.equals(current);
        }

        public boolean compareAndSet(ReplicatorActivityLevel current) {

            boolean isChanged = isStatusChanged(current);
            if(isChanged) {
                this.setLastStatus(current);
            }
            return isChanged;
        }
    }
}
