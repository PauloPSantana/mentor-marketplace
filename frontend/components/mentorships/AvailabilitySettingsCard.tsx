"use client";

import { FormEvent, useEffect, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import {
  getMentorAvailability,
  saveMentorAvailability,
  type AvailabilityRule,
  type MentorAvailability
} from "@/lib/availability";

const WEEKDAYS: AvailabilityRule["dayOfWeek"][] = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"];
const WEEKEND: AvailabilityRule["dayOfWeek"][] = ["SATURDAY", "SUNDAY"];
const LABELS: Record<AvailabilityRule["dayOfWeek"], string> = {
  MONDAY: "Seg",
  TUESDAY: "Ter",
  WEDNESDAY: "Qua",
  THURSDAY: "Qui",
  FRIDAY: "Sex",
  SATURDAY: "Sáb",
  SUNDAY: "Dom"
};

function defaultRules(): AvailabilityRule[] {
  return WEEKDAYS.map((dayOfWeek) => ({
    dayOfWeek,
    startTime: "09:00",
    endTime: "18:00",
    active: true
  }));
}

type AvailabilitySettingsCardProps = {
  mentorId: string;
};

export function AvailabilitySettingsCard({ mentorId }: AvailabilitySettingsCardProps) {
  const [availability, setAvailability] = useState<MentorAvailability | null>(null);
  const [rules, setRules] = useState<AvailabilityRule[]>(defaultRules());
  const [duration, setDuration] = useState(60);
  const [buffer, setBuffer] = useState(15);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getMentorAvailability(mentorId)
      .then((loaded) => {
        setAvailability(loaded);
        setDuration(loaded.slotDurationMinutes);
        setBuffer(loaded.bufferMinutes);
        if (loaded.rules.length > 0) {
          setRules(loaded.rules);
        }
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar a disponibilidade.")));
  }, [mentorId]);

  function toggleDay(dayOfWeek: AvailabilityRule["dayOfWeek"]) {
    setRules((current) => {
      const existing = current.find((rule) => rule.dayOfWeek === dayOfWeek);
      if (existing) {
        return current.map((rule) =>
          rule.dayOfWeek === dayOfWeek ? { ...rule, active: !rule.active } : rule
        );
      }
      return [
        ...current,
        { dayOfWeek, startTime: "09:00", endTime: "18:00", active: true }
      ];
    });
  }

  function updateTime(dayOfWeek: AvailabilityRule["dayOfWeek"], field: "startTime" | "endTime", value: string) {
    setRules((current) =>
      current.map((rule) => (rule.dayOfWeek === dayOfWeek ? { ...rule, [field]: value } : rule))
    );
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setMessage(null);
    setSaving(true);
    try {
      const saved = await saveMentorAvailability(mentorId, {
        slotDurationMinutes: duration,
        bufferMinutes: buffer,
        rules: [...WEEKDAYS, ...WEEKEND]
          .map((day) => rules.find((rule) => rule.dayOfWeek === day))
          .filter((rule): rule is AvailabilityRule => Boolean(rule?.active))
      });
      setAvailability(saved);
      setMessage("Disponibilidade salva. O mentorado verá somente os horários livres.");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível salvar a disponibilidade."));
    } finally {
      setSaving(false);
    }
  }

  const activeDays = new Set(rules.filter((rule) => rule.active).map((rule) => rule.dayOfWeek));

  return (
    <section className="enrollment-section">
      <h2>Disponibilidade</h2>
      <p className="post-meta">
        Defina os dias em que atende. O Mentor Marketplace cruza isso com o Google Agenda e mostra ao mentorado só os
        horários livres, sem os títulos dos seus compromissos.
      </p>
      <form className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "0.75rem" }}>
        <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }}>
          {[...WEEKDAYS, ...WEEKEND].map((day) => (
            <label key={day} style={{ display: "flex", alignItems: "center", gap: "0.35rem" }}>
              <input type="checkbox" checked={activeDays.has(day)} onChange={() => toggleDay(day)} />
              {LABELS[day]}
            </label>
          ))}
        </div>
        {rules
          .filter((rule) => rule.active)
          .map((rule) => (
            <div key={rule.dayOfWeek} style={{ display: "flex", gap: "0.75rem", alignItems: "center", flexWrap: "wrap" }}>
              <span className="post-meta" style={{ minWidth: "2.5rem" }}>
                {LABELS[rule.dayOfWeek]}
              </span>
              <input
                className="input"
                type="time"
                value={rule.startTime.slice(0, 5)}
                onChange={(event) => updateTime(rule.dayOfWeek, "startTime", event.target.value)}
              />
              <span className="post-meta">às</span>
              <input
                className="input"
                type="time"
                value={rule.endTime.slice(0, 5)}
                onChange={(event) => updateTime(rule.dayOfWeek, "endTime", event.target.value)}
              />
            </div>
          ))}
        <label>
          Duração da sessão (minutos)
          <input className="input" type="number" min={15} max={240} value={duration} onChange={(event) => setDuration(Number(event.target.value))} />
        </label>
        <label>
          Intervalo entre mentorias (minutos)
          <input className="input" type="number" min={0} max={120} value={buffer} onChange={(event) => setBuffer(Number(event.target.value))} />
        </label>
        {error ? <p className="error">{error}</p> : null}
        {message ? <p style={{ color: "var(--accent)" }}>{message}</p> : null}
        <button className="btn" type="submit" disabled={saving}>
          {saving ? "Salvando..." : "Salvar disponibilidade"}
        </button>
      </form>
      {availability && availability.days.some((day) => day.slots.length > 0) ? (
        <p className="post-meta" style={{ marginTop: "0.75rem" }}>
          Próximos horários livres: {availability.days.flatMap((day) => day.slots).slice(0, 6).map((slot) => slot.label).join(", ")}
        </p>
      ) : null}
    </section>
  );
}
