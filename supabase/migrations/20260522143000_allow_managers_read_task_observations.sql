-- Allow gestores to read observations attached to task records in their assigned projects.

drop policy if exists "Managers can read assigned task observations" on public.observacoes;

create policy "Managers can read assigned task observations"
on public.observacoes
for select
to authenticated
using (
  public.current_user_is_gestor()
  and exists (
    select 1
    from public.registos_tarefa rt
    where rt.id = observacoes.registo_id
      and public.current_user_manages_task(rt.tarefa_id)
  )
);
