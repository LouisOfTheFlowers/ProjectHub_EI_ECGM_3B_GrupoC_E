-- Allow gestores to read photos attached to observations for tasks in their assigned projects.

drop policy if exists "Managers can read assigned task observation photos" on public.observacao_fotos;

create policy "Managers can read assigned task observation photos"
on public.observacao_fotos
for select
to authenticated
using (
  public.current_user_is_gestor()
  and exists (
    select 1
    from public.observacoes o
    inner join public.registos_tarefa rt
      on rt.id = o.registo_id
    where o.id = observacao_fotos.observacao_id
      and public.current_user_manages_task(rt.tarefa_id)
  )
);
