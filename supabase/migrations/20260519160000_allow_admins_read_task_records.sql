-- Allow admin users to read task records used by executive reports.

drop policy if exists "Admins can read task records" on public.registos_tarefa;

create policy "Admins can read task records"
on public.registos_tarefa
for select
to authenticated
using (public.current_user_is_admin());
