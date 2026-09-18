package br.com.mentorhub.mentorships.api.dto;

public record SessionStatsResponse(
        long scheduledCount,
        long completedCount,
        long cancelledCount,
        long noShowCount,
        long completedMinutes,
        double completionRate,
        double attendanceRate
) {
}
