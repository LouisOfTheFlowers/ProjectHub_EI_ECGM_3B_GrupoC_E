-- Allow gestores to export task record statistics for tasks in their assigned projects.

drop policy if exists "Managers can read assigned task records" on public.registos_tarefa;

create policy "Managers can read assigned task records"
on public.registos_tarefa
for select
to authenticated
using (
  public.current_user_is_gestor()
  and public.current_user_manages_task(tarefa_id)
);
